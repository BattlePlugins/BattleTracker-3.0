package org.battleplugins.tracker.sign;

import lombok.Getter;

import mc.alk.mc.MCLocation;

/**
 * Holds information about tracker signs.
 *
 * @author Redned
 */
@Getter
public class LeaderboardSign {

    /**
     * The location of the sign
     *
     * @return the location of the sign
     */
    private MCLocation location;

    /**
     * The stat type displayed on the sign
     *
     * @return the stat type displayed on the sign
     */
    private String statType;

    /**
     * The tracker name to retrieve information from
     * when displaying information on the sign
     *
     * @return the tracker name to retrieve information from
     */
    private String trackerName;

    public LeaderboardSign(MCLocation location, String statType, String trackerName) {
        this.location = location;
        this.statType = statType;
        this.trackerName = trackerName;
    }
}
