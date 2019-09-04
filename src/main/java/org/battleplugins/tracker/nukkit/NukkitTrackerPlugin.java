package org.battleplugins.tracker.nukkit;

import cn.nukkit.plugin.service.ServicePriority;
import mc.alk.battlecore.nukkit.NukkitBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.nukkit.listener.TrackerListener;
import org.battleplugins.tracker.nukkit.listener.PvEListener;
import org.battleplugins.tracker.nukkit.listener.PvPListener;

/**
 * Main class for BattleTracker Nukkit.
 *
 * @author Redned
 */
public class NukkitTrackerPlugin extends NukkitBattlePlugin {

    private BattleTracker tracker;

    @Override
    public void onEnable() {
        super.onEnable();

        tracker = new BattleTracker(this);
        BattleTracker.setInstance(tracker);
        // Register the tracker manager into the service provider API
        getServer().getServiceManager().register(TrackerManager.class, tracker.getTrackerManager(), this, ServicePriority.NORMAL);

        if (tracker.getTrackerManager().isTrackingPvE()) {
            getServer().getPluginManager().registerEvents(new PvEListener(tracker), this);
        }

        if (tracker.getTrackerManager().isTrackingPvP()) {
            getServer().getPluginManager().registerEvents(new PvPListener(tracker), this);
        }

        getServer().getPluginManager().registerEvents(new TrackerListener(tracker), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

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
