package org.battleplugins.tracker.tracking.stat;

/**
 * An enum of the default StatTypes
 * for BattleTracker.
 *
 * @author Redned
 */
public enum StatType {

    KILLS("Kills", true),
    DEATHS("Deaths", true),
    TIES("Ties", true),
    STREAK("Streak", false),
    MAX_STREAK("Max Streak", true),
    RANKING("Ranking", false),
    MAX_RANKING("Max Ranking", true),
    RATING("Rating", true),
    MAX_RATING("Max Rating", true),
    KD_RATIO("K/D Ratio", false),
    MAX_KD_RATIO("Max K/D Ratio", true);

    private String name;
    private boolean track;

    StatType(String name, boolean track) {
        this.name = name;
        this.track = track;
    }

    /**
     * Returns the internal name of the stat type, this
     * is what's used when storing data in a database
     *
     * @return the internal name of the stat type
     */
    public String getInternalName() {
        return name().toLowerCase();
    }

    /**
     * Returns the name of the stat type
     *
     * @return the name of the stat type
     */
    public String getName() {
        return name;
    }

    /**
     * Returns if the stat type should be tracked/
     * stored inside of the database
     *
     * @return if the stat type should be tracked
     */
    public boolean isTracking() {
        return track;
    }
}
