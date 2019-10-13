package org.battleplugins.tracker.bukkit.listener;

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
import org.battleplugins.tracker.util.Util;
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

/**
 * Miscellaneous listener for BattleTracker in Bukkit.
 *
 * @author Redned
 */
public class TrackerListener implements Listener {

    private BattleTracker tracker;

    public TrackerListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when player joins
     *
     * @param event the event being called
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TrackerInterface pvpInterface = tracker.getTrackerManager().getPvPInterface();
        TrackerInterface pveInterface = tracker.getTrackerManager().getPvEInterface();

        MCOfflinePlayer offlinePlayer = tracker.getPlatform().getOfflinePlayer(event.getPlayer().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvE()) {
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
        for (Map.Entry<String, TrackerInterface> interfaces : tracker.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(tracker.getPlatform().getOfflinePlayer(event.getPlayer().getUniqueId()));
        }

        for (TrackerInterface trackerInterface : tracker.getTrackerManager().getInterfaces().values()) {
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
        MCPlayer player = tracker.getPlatform().getPlayer(event.getPlayer().getName());
        MCSign sign = new BukkitSign((Sign) event.getBlock().getState());
        if (!SignUtil.isLeaderboardSign(event.getLines()))
            return;

        if (!event.getPlayer().hasPermission("battletracker.sign")) {
            event.setCancelled(true); // cancel anyway
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType(), 1));
            player.sendMessage(tracker.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(event.getLines());
        String trackerName = SignUtil.getTrackerName(event.getLines());

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        tracker.getSignManager().addSign(leaderboardSign);

        tracker.getPlatform().scheduleSyncTask(tracker, () -> {
            MCSign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockAt(sign.getLocation()), MCSign.class);
            tracker.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(tracker.getMessageManager().getFormattedMessage("createdNewSign"));
    }

    /**
     * Event called when a sign is clicked
     *
     * @param event the event being called
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        MCPlayer player = tracker.getPlatform().getPlayer(event.getPlayer().getName());
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
            return;

        Location bukkitLocation = event.getClickedBlock().getLocation();
        MCLocation location = tracker.getPlatform().getLocation(bukkitLocation.getWorld().getName(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
        LeaderboardSign leaderboardSign = tracker.getSignManager().getSigns().get(location);
        if (leaderboardSign == null)
            return; // not a BattleTracker sign

        Record record = tracker.getTrackerManager().getInterface(leaderboardSign.getTrackerName()).getRecord(player);
        String[] lines = tracker.getSignManager().getPersonalFormat().clone();
        for (String line : lines) {
            Util.replaceRecordValues(line, record);
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
