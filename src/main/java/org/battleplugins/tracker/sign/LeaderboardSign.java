package org.battleplugins.tracker.sign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.battleplugins.api.world.Location;

/**
 * Holds information about tracker signs.
 *
 * @author Redned
 */
@Getter
@AllArgsConstructor
public class LeaderboardSign {

    /**
     * The location of the sign
     *
     * @return the location of the sign
     */
    private Location location;

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
}
