package org.battleplugins.tracker;

import mc.alk.mc.plugin.MCPlugin;
import org.battleplugins.tracker.executor.TrackerExecutor;
import org.battleplugins.tracker.impl.Tracker;
import org.battleplugins.tracker.stat.calculator.EloCalculator;
import org.battleplugins.tracker.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.stat.record.Record;

import java.util.HashMap;

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

    private RatingCalculator defaultCalculator;

    public BattleTracker(MCPlugin platform) {
        this.platform = platform;
        this.trackerManager = new TrackerManager();

        // TODO: Retrieve this info from the config
        this.defaultCalculator = new EloCalculator(1250, 400);

        // TODO: Set values for this from config and database
        trackerManager.addInterface(PVP_INTERFACE, new Tracker(PVP_INTERFACE, defaultCalculator, new HashMap<>()));
        trackerManager.addInterface(PVE_INTERFACE, new Tracker(PVE_INTERFACE, defaultCalculator, new HashMap<>()));

        platform.registerMCCommand(PVP_INTERFACE.toLowerCase(), new TrackerExecutor(this, PVP_INTERFACE));
        platform.registerMCCommand(PVE_INTERFACE.toLowerCase(), new TrackerExecutor(this, PVE_INTERFACE));
    }

    /**
     * Returns the TrackerManager instance
     *
     * @return the TrackerManager instance
     */
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    /**
     * Returns the default rating calculator
     *
     * @return the default rating calculator
     */
    public RatingCalculator getDefaultCalculator() {
        return defaultCalculator;
    }

    /**
     * Returns the current platform 
     *
     * @return the current platform
     */
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
