package org.battleplugins.tracker;

import mc.alk.mc.plugin.MCPlugin;

/**
 * Overall main class for the BattleTracker plugin.
 *
 * @author Zach443, Redned
 */
public final class BattleTracker {

    public static final String PVP_INTERFACE = "PvP";
    public static final String PVE_INTERFACE = "PvE";

    private static BattleTracker instance;

    private MCPlugin platform;
    private TrackerManager trackerManager;

    public BattleTracker(MCPlugin platform) {
        this.platform = platform;
        this.trackerManager = new TrackerManager();
    }

    /**
     * Returns the TrackerManager instance
     *
     * @return the TrackerManager instance
     */
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    public MCPlugin getPlatform() {
        return platform;
    }

    /**
     * Returns the current BattleTracker instance
     *
     * @return the current BattleTracker instance
     */
    public static BattleTracker getInstance() {
        return instance;
    }

    /**
     * Sets the current BattleTracker singleton. Cannot
     * be done if it's already set
     *
     * @param tracker the BattleTracker instance to set
     */
    public static void setInstance(BattleTracker tracker) {
        if (instance != null)
            throw new UnsupportedOperationException("Cannot redefine singleton BattleTracker!");

            instance = tracker;

    }
}
