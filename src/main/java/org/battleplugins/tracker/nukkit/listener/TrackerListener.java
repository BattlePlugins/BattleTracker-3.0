package org.battleplugins.tracker.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import mc.alk.mc.MCServer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.record.PlayerRecord;

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
        if (!pvpInterface.hasRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()))) {
            pvpInterface.createNewRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()), new PlayerRecord(pvpInterface, event.getPlayer().getName()));
        }

        if (!pveInterface.hasRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()))) {
            pveInterface.createNewRecord(MCServer.getOfflinePlayer(event.getPlayer().getName()), new PlayerRecord(pveInterface, event.getPlayer().getName()));
        }
    }
}
