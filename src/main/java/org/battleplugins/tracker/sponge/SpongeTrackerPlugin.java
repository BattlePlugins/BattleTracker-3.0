package org.battleplugins.tracker.sponge;

import mc.alk.battlecore.sponge.SpongeBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInfo;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.sponge.listener.PvEListener;
import org.battleplugins.tracker.sponge.listener.PvPListener;
import org.battleplugins.tracker.sponge.listener.TrackerListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

/**
 * Main class for BattleTracker Sponge.
 *
 * @author Redned
 */
@Plugin(id = "bt", authors = "BattlePlugins", name = TrackerInfo.NAME, version = TrackerInfo.VERSION, description = TrackerInfo.DESCRIPTION, url = TrackerInfo.URL)
public class SpongeTrackerPlugin extends SpongeBattlePlugin {

    private BattleTracker tracker;

    @Override
    public void onEnable() {
        super.onEnable();

        tracker = new BattleTracker(this);
        BattleTracker.setInstance(tracker);

        // Register the tracker manager into the service provider API
        Sponge.getServiceManager().setProvider(this, TrackerManager.class, tracker.getTrackerManager());

        if (tracker.getTrackerManager().isTrackingPvE()) {
            Sponge.getEventManager().registerListeners(this, new PvEListener(tracker));
        }

        if (tracker.getTrackerManager().isTrackingPvP()) {
            Sponge.getEventManager().registerListeners(this, new PvPListener(tracker));
        }

        Sponge.getEventManager().registerListeners(this, new TrackerListener(tracker));
    }

    @Override
    public void onDisable() {
        getMCLogger().info("Saving all records...");
        try {
            for (TrackerInterface trackerInterface : tracker.getTrackerManager().getInterfaces().values()) {
                trackerInterface.saveAll();
            }
            getMCLogger().info("Saved all records successfully!");
        } catch (Exception ex) {
            getMCLogger().error("Could not save all records! Please make sure everything is configured correctly!");
            getMCLogger().error("If this error persists, please open a report on GitHub!");
            ex.printStackTrace();
        }
    }
}
