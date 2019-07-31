package org.battleplugins.tracker.stat.record;

import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;

import java.util.Map;

/**
 * Stores and holds tracker data before it
 * is put into the database.
 *
 * @author Redned
 */
public abstract class Record {

    protected TrackerInterface tracker;

    protected String id;
    protected float rating;

    protected Map<String, Integer> stats;

    protected boolean tracking;

    public Record(TrackerInterface tracker, String id, Map<String, Integer> stats, int rating) {
        this.tracker = tracker;

        this.id = id;
        this.rating = rating;
        this.tracking = true;
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
    public int getStat(StatType stat) {
        return getStat(stat.getInternalName());
    }

    /**
     * Returns the value for the specified stat
     *
     * @param stat the stat to get the value of
     * @return the value for the specified stat
     */
    public int getStat(String stat) {
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
    public void setValue(String stat, int value) {
        stats.put(stat, value);
    }

    /**
     * Returns a map of all the stats
     *
     * Key: the stat type
     * Value: the stat value
     *
     * @return a map of all the stats
     */
    public Map<String, Integer> getStats() {
        return stats;
    }

    /**
     * Returns the ID of the record
     *
     * @return the ID of the record
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the ID of the record
     *
     * @param id the ID of the record
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Returns the rating of the record
     *
     * @return the rating of the record
     */
    public float getRating() {
        return rating;
    }

    /**
     * Sets the rating of the record
     *
     * @param rating the rating of the record
     */
    public void setRating(float rating) {
        this.rating = rating;
    }

    /**
     * Returns if this record should be tracked
     *
     * @return if this record should be tracked
     */
    public boolean isTracking() {
        return tracking;
    }

    /**
     * Sets if this record should be tracked
     *
     * @param tracking if this record should be tracked
     */
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
}
