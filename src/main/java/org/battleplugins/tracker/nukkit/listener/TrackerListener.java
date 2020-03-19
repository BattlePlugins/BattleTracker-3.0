package org.battleplugins.tracker.nukkit.listener;

import lombok.AllArgsConstructor;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;

import org.battleplugins.api.entity.living.player.OfflinePlayer;
import org.battleplugins.api.entity.living.player.Player;
import org.battleplugins.api.nukkit.world.block.entity.NukkitSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.util.SignUtil;

import java.util.Map;

/**
 * Miscellaneous listener for BattleTracker in Nukkit.
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

        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(event.getPlayer().getUniqueId()).get();
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
            interfaces.getValue().save(plugin.getServer().getOfflinePlayer(event.getPlayer().getUniqueId()).get());
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
        Player player = plugin.getServer().getPlayer(event.getPlayer().getName()).get();
        org.battleplugins.api.world.block.entity.Sign sign = new NukkitSign((BlockEntitySign) event.getBlock().getLevel().getBlockEntity(event.getBlock().getLocation()));
        if (!SignUtil.isLeaderboardSign(event.getLines()))
            return;

        if (!event.getPlayer().hasPermission("battletracker.sign")) {
            event.setCancelled(true); // cancel anyway
            event.getBlock().getLevel().setBlock(event.getBlock().getLocation(), Block.get(Block.AIR));
            event.getBlock().getLevel().dropItem(event.getBlock().getLocation(), Item.get(event.getBlock().getId()));
            player.sendMessage(plugin.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(event.getLines());
        String trackerName = SignUtil.getTrackerName(event.getLines());

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        plugin.getSignManager().addSign(leaderboardSign);

        plugin.getPlatform().scheduleSyncTask(plugin, () -> {
            org.battleplugins.api.world.block.entity.Sign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockEntityAt(sign.getLocation()).get(), org.battleplugins.api.world.block.entity.Sign.class);
            plugin.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(plugin.getMessageManager().getFormattedMessage("createdNewSign"));
    }
}
