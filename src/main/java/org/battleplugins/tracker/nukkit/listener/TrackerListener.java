package org.battleplugins.tracker.nukkit.listener;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Location;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.block.MCSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.stat.record.Record;
import org.battleplugins.tracker.util.Util;

import java.util.Map;

/**
 * Miscellaneous listener for BattleTracker in Nukkit.
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

        MCOfflinePlayer offlinePlayer = MCPlatform.getOfflinePlayer(event.getPlayer().getUniqueId());
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
            interfaces.getValue().save(MCPlatform.getOfflinePlayer(event.getPlayer().getUniqueId()));
        }
    }

    /**
     * Event called when a sign is clicked
     *
     * @param event the event being called
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        MCPlayer player = MCPlatform.getPlayer(event.getPlayer().getName());

        if (event.getBlock() == null || event.getBlock().getId() == Block.AIR)
            return;

        Location nukkitLocation = event.getBlock().getLocation();
        MCLocation location = MCPlatform.getLocation(nukkitLocation.getLevel().getName(), nukkitLocation.getX(), nukkitLocation.getY(), nukkitLocation.getZ());
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
}
