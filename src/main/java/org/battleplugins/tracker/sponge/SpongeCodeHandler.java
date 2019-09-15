package org.battleplugins.tracker.sponge;

import mc.alk.mc.plugin.platform.PlatformCodeHandler;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.sponge.listener.PvEListener;
import org.battleplugins.tracker.sponge.listener.PvPListener;
import org.battleplugins.tracker.sponge.listener.TrackerListener;
import org.spongepowered.api.Sponge;

/**
 * Handler for version-dependent Sponge code.
 *
 * @author Redned
 */
public class SpongeCodeHandler extends PlatformCodeHandler {

    private BattleTracker tracker;

    public SpongeCodeHandler(BattleTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onEnable() {
        // Register the tracker manager into the service provider API
        Sponge.getServiceManager().setProvider(tracker.getPlatformPlugin(), TrackerManager.class, tracker.getTrackerManager());

        if (tracker.getTrackerManager().isTrackingPvE()) {
            Sponge.getEventManager().registerListeners(tracker.getPlatformPlugin(), new PvEListener(tracker));
        }

        if (tracker.getTrackerManager().isTrackingPvP()) {
            Sponge.getEventManager().registerListeners(tracker.getPlatformPlugin(), new PvPListener(tracker));
        }

        Sponge.getEventManager().registerListeners(tracker.getPlatformPlugin(), new TrackerListener(tracker));
    }
}
