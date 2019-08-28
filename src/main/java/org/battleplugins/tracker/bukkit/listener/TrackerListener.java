package org.battleplugins.tracker.bukkit.listener;

import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCServer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

        MCOfflinePlayer offlinePlayer = MCServer.getOfflinePlayer(event.getPlayer().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvE()) {
            pveInterface.createNewRecord(offlinePlayer);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Map.Entry<String, TrackerInterface> interfaces : tracker.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(MCServer.getOfflinePlayer(event.getPlayer().getUniqueId()));
        }
    }
}
