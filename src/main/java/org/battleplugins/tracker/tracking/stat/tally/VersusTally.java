package org.battleplugins.tracker.tracking.stat.tally;

import org.battleplugins.tracker.tracking.TrackerInterface;

import java.util.Map;

import mc.alk.mc.MCOfflinePlayer;

/**
 * A tally storing versus information about
 * two players.
 *
 * @author Redned
 */
public class VersusTally {

    private TrackerInterface tracker;

    private String id1;
    private String name1;

    private String id2;
    private String name2;

    private Map<String, Float> stats;

    public VersusTally(TrackerInterface tracker, MCOfflinePlayer player1, MCOfflinePlayer player2, Map<String, Float> stats) {
        this(tracker, player1.getUniqueId().toString(), player2.getUniqueId().toString(), player1.getName(), player2.getName(), stats);
    }

    public VersusTally(TrackerInterface tracker, String id1, String id2, String name1, String name2, Map<String, Float> stats) {
        this.tracker = tracker;
        this.id1 = id1;
        this.id2 = id2;
        this.name1 = name1;
        this.name2 = name2;

        this.stats = stats;
    }

    /**
     * Returns the UUID of the first player in the tally
     *
     * @return the UUID of the first player in the tally
     */
    public String getId1() {
        return id1;
    }

    /**
     * Returns the UUID of the second player in the tally
     *
     * @return the UUID of the second player in the tally
     */
    public String getId2() {
        return id2;
    }

    /**
     * Returns the name of the first player in the tally
     *
     * @return the name of the first player in the tally
     */
    public String getName1() {
        return name1;
    }

    /**
     * Returns the name of the second player in the tally
     *
     * @return the name of the second player in the tally
     */
    public String getName2() {
        return name2;
    }

    /**
     * Returns the stats for this tally
     *
     * Key: the name of the stat
     * Value: the value of the stat
     *
     * @return the stats for this tally
     */
    public Map<String, Float> getStats() {
        return stats;
    }
}
