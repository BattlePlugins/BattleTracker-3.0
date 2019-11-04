package org.battleplugins.tracker.bukkit.listener;

import lombok.AllArgsConstructor;

import mc.alk.battlecore.util.Log;
import mc.alk.bukkit.block.BukkitSign;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.block.MCSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.SignUtil;
import org.battleplugins.tracker.util.TrackerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * Miscellaneous listener for BattleTracker in Bukkit.
 *
 * @author Redned
 */
@AllArgsConstructor
public class TrackerListener implements Listener {

    private BattleTracker plugin;

    /**
     * Event called when player joins
     *
     * @param event the event being called
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TrackerInterface pvpInterface = plugin.getTrackerManager().getPvPInterface();
        TrackerInterface pveInterface = plugin.getTrackerManager().getPvEInterface();

        MCOfflinePlayer offlinePlayer = plugin.getPlatform().getOfflinePlayer(event.getPlayer().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && plugin.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && plugin.getTrackerManager().isTrackingPvE()) {
            pveInterface.createNewRecord(offlinePlayer);
        }
    }

    /**
     * Event called when player quits
     *
     * @param event the event being called
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Map.Entry<String, TrackerInterface> interfaces : plugin.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(plugin.getPlatform().getOfflinePlayer(event.getPlayer().getUniqueId()));
        }

        for (TrackerInterface trackerInterface : plugin.getTrackerManager().getInterfaces().values()) {
            if (!trackerInterface.getRecapManager().getDeathRecaps().containsKey(event.getPlayer().getName()))
                continue;

            trackerInterface.getRecapManager().getDeathRecaps().remove(event.getPlayer().getName());
        }
    }

    /**
     * Event called when a sign is changed
     *
     * @param event the event being called
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        MCPlayer player = plugin.getPlatform().getPlayer(event.getPlayer().getName());
        MCSign sign = new BukkitSign((Sign) event.getBlock().getState());
        if (!SignUtil.isLeaderboardSign(event.getLines()))
            return;

        if (!event.getPlayer().hasPermission("battletracker.sign")) {
            event.setCancelled(true); // cancel anyway
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType(), 1));
            player.sendMessage(plugin.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(event.getLines());
        String trackerName = SignUtil.getTrackerName(event.getLines());

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        plugin.getSignManager().addSign(leaderboardSign);

        plugin.getPlatform().scheduleSyncTask(plugin, () -> {
            MCSign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockAt(sign.getLocation()), MCSign.class);
            plugin.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(plugin.getMessageManager().getFormattedMessage("createdNewSign"));
    }

    /**
     * Event called when a sign is clicked
     *
     * @param event the event being called
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        MCPlayer player = plugin.getPlatform().getPlayer(event.getPlayer().getName());
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
            return;

        Location bukkitLocation = event.getClickedBlock().getLocation();
        MCLocation location = plugin.getPlatform().getLocation(bukkitLocation.getWorld().getName(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
        LeaderboardSign leaderboardSign = plugin.getSignManager().getSigns().get(location);
        if (leaderboardSign == null)
            return; // not a BattleTracker sign

        Optional<TrackerInterface> opTracker = plugin.getTrackerManager().getInterface(leaderboardSign.getTrackerName());
        if (!opTracker.isPresent())  {
            Log.debug("A tracker could not be found for " + leaderboardSign.getTrackerName() + "!");
            return;
        }

        Record record = opTracker.get().getOrCreateRecord(player);
        String[] lines = plugin.getSignManager().getPersonalFormat().clone();
        for (String line : lines) {
            TrackerUtil.replaceRecordValues(line, record);
        }

        MCSign sign = (MCSign) location.getWorld().getBlockAt(location);
        sign.sendSignChange(player, lines);
    }

    /**
     * Event called when a player clicks in a recap inventory
     *
     * @param event the event being called
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("'s Recap"))
            event.setCancelled(true);
    }
}
