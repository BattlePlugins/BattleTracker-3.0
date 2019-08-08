package org.battleplugins.tracker.bukkit.listener;

import mc.alk.mc.MCServer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.record.PlayerRecord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
        if (!pvpInterface.hasRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()))) {
            pvpInterface.createNewRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()), new PlayerRecord(pvpInterface, event.getPlayer().getName()));
        }

        if (!pveInterface.hasRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()))) {
            pveInterface.createNewRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()), new PlayerRecord(pveInterface, event.getPlayer().getName()));
        }
    }
}
