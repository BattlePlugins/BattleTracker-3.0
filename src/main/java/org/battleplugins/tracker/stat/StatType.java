package org.battleplugins.tracker.stat;

/**
 * An enum of the default StatTypes
 * for BattleTracker.
 *
 * @author Redned
 */
public enum StatType {

    KILLS("Kills"),
    DEATHS("Deaths"),
    TIES("Ties"),
    STREAK("Streak"),
    MAX_STREAK("Max Streak"),
    RANKING("Ranking"),
    MAX_RANKING("Max Ranking"),
    RATING("Rating"),
    MAX_RATING("Max Rating"),
    KD_RATIO("K/D Ratio");

    private String name;

    StatType(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return name().toLowerCase();
    }

    public String getName() {
        return name;
    }
}
