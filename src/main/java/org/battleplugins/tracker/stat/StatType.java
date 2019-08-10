package org.battleplugins.tracker.stat;

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

    public String getInternalName() {
        return name().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public boolean isTracking() {
        return track;
    }
}
