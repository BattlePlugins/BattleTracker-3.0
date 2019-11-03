package org.battleplugins.tracker.tracking.stat.record;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.StatTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and holds tracker data before it
 * is put into the database.
 *
 * @author Redned
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class Record {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @NonNull
    protected TrackerInterface tracker;

    /**
     * The ID of the record
     *
     * @param id the ID of the record
     * @return the ID of the record
     */
    @NonNull
    protected String id;

    /**
     * The name of the record
     *
     * @param name the name of the record
     * @return the name of the record
     */
    @NonNull
    protected String name;

    /**
     * A map of all the stats
     *
     * Key: the stat type
     * Value: the stat value
     *
     * @return a map of all the stats
     */
    @Setter(AccessLevel.NONE)
    @NonNull
    protected Map<String, Float> stats;

    /**
     * Returns if this record should be tracked
     *
     * @param tracking if this record should be tracked
     * @return if this record should be tracked
     */
    private boolean tracking = true;

    /**
     * Returns if tracker messages should be sent for this record
     *
     * @param sendingMessages if tracker messages should be sent for this record
     * @return if tracker messages should be sent for this record
     */
    private boolean sendingMessages = true;

    public Record(TrackerInterface tracker, String id, String name) {
        this(tracker, id, name, new HashMap<>());
    }

    /**
     * Returns if the stat is in the record
     *
     * @param stat the stat to check
     * @return if the stat is in the record
     */
    public boolean hasStat(String stat) {
        return stats.containsKey(stat);
    }

    /**
     * Returns the value for the specified StatType
     *
     * @param stat the StatType to get the value of
     * @return the value for the specified StatType
     */
    public float getStat(StatType stat) {
        return getStat(stat.getInternalName());
    }

    /**
     * Returns the value for the specified stat
     *
     * @param stat the stat to get the value of
     * @return the value for the specified stat
     */
    public float getStat(String stat) {
        if (!stats.containsKey(stat))
            return 0;

        return stats.get(stat);
    }

    /**
     * Sets the value of the given stat
     *
     * @param stat the stat to set the value for
     * @param value the (new) value of the stat
     */
    public void setValue(String stat, float value) {
        stats.put(stat, value);
    }

    /**
     * Returns the rating of the record
     *
     * @return the rating of the record
     */
    public float getRating() {
        return stats.get(StatTypes.RATING.getInternalName());
    }

    /**
     * Sets the rating of the record
     *
     * @param rating the rating of the record
     */
    public void setRating(float rating) {
        stats.put(StatTypes.RATING.getInternalName(), rating);
    }
}
