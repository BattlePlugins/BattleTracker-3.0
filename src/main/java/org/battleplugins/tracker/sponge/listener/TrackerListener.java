package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlatform;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Map;

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

        MCOfflinePlayer offlinePlayer = MCPlatform.getOfflinePlayer(event.getTargetEntity().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvE()) {
            pveInterface.createNewRecord(offlinePlayer);
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        for (Map.Entry<String, TrackerInterface> interfaces : tracker.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(MCPlatform.getOfflinePlayer(event.getTargetEntity().getUniqueId()));
        }
    }
}
