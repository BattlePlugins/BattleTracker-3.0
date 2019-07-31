package org.battleplugins.tracker;

import mc.alk.v1r7.core.MCPlugin;
import org.bukkit.plugin.ServicePriority;

/**
 * Main class for the BattleTracker plugin.
 *
 * @author Zach443, Redned
 */
public class BattleTracker extends MCPlugin {

    public static final String PVP_INTERFACE = "PvP";
    public static final String PVE_INTERFACE = "PvE";

    private TrackerManager trackerManager;

    @Override
    public void onEnable() {
        this.trackerManager = new TrackerManager();

        // Register the tracker manager into the service provider API
        getServer().getServicesManager().register(TrackerManager.class, trackerManager, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {

    }

    /**
     * Returns the TrackerManager instance
     *
     * @return the TrackerManager instance
     */
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }
}
