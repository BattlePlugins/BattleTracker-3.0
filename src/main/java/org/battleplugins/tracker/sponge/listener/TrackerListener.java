package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCServer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.record.PlayerRecord;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Miscellaneous listener for BattleTracker in Sponge.
 *
 * @author Redned
 */
public class TrackerListener {

    private BattleTracker tracker;

    public TrackerListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when player joins
     *
     * @param event the event being called
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        TrackerInterface pvpInterface = tracker.getTrackerManager().getPvPInterface();
        TrackerInterface pveInterface = tracker.getTrackerManager().getPvEInterface();
        if (!pvpInterface.hasRecord(MCServer.getOfflinePlayer(event.getTargetEntity().getName()))) {
            pvpInterface.createNewRecord(MCServer.getOfflinePlayer(event.getTargetEntity().getName()), new PlayerRecord(pvpInterface, event.getTargetEntity().getName()));
        }

        if (!pveInterface.hasRecord(MCServer.getOfflinePlayer(event.getTargetEntity().getName()))) {
            pveInterface.createNewRecord(MCServer.getOfflinePlayer(event.getTargetEntity().getName()), new PlayerRecord(pveInterface, event.getTargetEntity().getName()));
        }
    }
}
