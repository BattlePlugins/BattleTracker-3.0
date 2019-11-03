package org.battleplugins.tracker.tracking.stat.tally;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.tracking.TrackerInterface;

import java.util.Map;

/**
 * A tally storing versus information about
 * two players.
 *
 * @author Redned
 */
@Getter
@AllArgsConstructor
public class VersusTally {

    @Getter(AccessLevel.NONE)
    private TrackerInterface tracker;

    /**
     * The UUID of the first player in the tally
     *
     * @return the UUID of the first player in the tally
     */
    private String id1;

    /**
     * The name of the first player in the tally
     *
     * @return the name of the first player in the tally
     */
    private String name1;

    /**
     * The UUID of the second player in the tally
     *
     * @return the UUID of the second player in the tally
     */
    private String id2;

    /**
     * The name of the second player in the tally
     *
     * @return the name of the second player in the tally
     */
    private String name2;

    /**
     * The stats for this tally
     *
     * Key: the name of the stat
     * Value: the value of the stat
     *
     * @return the stats for this tally
     */
    private Map<String, Float> stats;

    public VersusTally(TrackerInterface tracker, MCOfflinePlayer player1, MCOfflinePlayer player2, Map<String, Float> stats) {
        this(tracker, player1.getUniqueId().toString(), player2.getUniqueId().toString(), player1.getName(), player2.getName(), stats);
    }
}
