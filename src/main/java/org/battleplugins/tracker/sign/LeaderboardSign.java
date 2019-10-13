package org.battleplugins.tracker.sign;

import mc.alk.mc.MCLocation;

/**
 * Holds information about tracker signs.
 *
 * @author Redned
 */
public class LeaderboardSign {

    private MCLocation location;

    private String statType;
    private String trackerName;

    public LeaderboardSign(MCLocation location, String statType, String trackerName) {
        this.location = location;
        this.statType = statType;
        this.trackerName = trackerName;
    }

    /**
     * Returns the location of the sign
     *
     * @return the location of the sign
     */
    public MCLocation getLocation() {
        return location;
    }

    /**
     * Returns the stat type displayed on the sign
     *
     * @return the stat type displayed on the sign
     */
    public String getStatType() {
        return statType;
    }

    /**
     * Returns the tracker name to retrieve information from
     * when displaying information on the sign
     *
     * @return the tracker name to retrieve information from
     */
    public String getTrackerName() {
        return trackerName;
    }
}
