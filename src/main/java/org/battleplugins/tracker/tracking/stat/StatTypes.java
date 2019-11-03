package org.battleplugins.tracker.tracking.stat;

import java.util.HashSet;
import java.util.Set;

/**
 * A class containing all the general stat types.
 *
 * An enum is not used here so developers wanting to create
 * their own stat types easily can.
 *
 * @author Redned
 */
public class StatTypes {

    static final Set<StatType> statTypes = new HashSet<>();

    public static final StatType KILLS = StatType.builder().name("Kills").tracked(true).build();
    public static final StatType DEATHS = StatType.builder().name("Deaths").tracked(true).build();
    public static final StatType TIES = StatType.builder().name("Ties").tracked(true).build();
    public static final StatType STREAK = StatType.builder().name("streak").tracked(false).build();
    public static final StatType MAX_STREAK = StatType.builder().name("Max Streak").tracked(true).build();
    public static final StatType RANKING = StatType.builder().name("Ranking").tracked(false).build();
    public static final StatType MAX_RANKING = StatType.builder().name("Max Ranking").tracked(true).build();
    public static final StatType RATING = StatType.builder().name("Rating").tracked(true).build();
    public static final StatType MAX_RATING = StatType.builder().name("Max Rating").tracked(true).build();
    public static final StatType KD_RATIO = StatType.builder().internalName("kd_ratio").name("K/D Ratio").tracked(false).build();
    public static final StatType MAX_KD_RATIO = StatType.builder().internalName("max_kd_ratio").name("Max K/D Ratio").tracked(true).build();

    /**
     * Returns a set of all the registered stat types
     *
     * @return a set of all the registered stat types
     */
    public static Set<StatType> values() {
        return statTypes;
    }
}
