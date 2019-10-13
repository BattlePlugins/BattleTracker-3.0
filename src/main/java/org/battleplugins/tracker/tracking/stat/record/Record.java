package org.battleplugins.tracker.tracking.stat.record;

import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.StatType;

import java.util.HashMap;
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
    protected String name;

    protected Map<String, Float> stats;

    protected boolean tracking;
    protected boolean sendMessages;

    public Record(TrackerInterface tracker, String id, String name) {
        this(tracker, id, name, new HashMap<>());
    }

    public Record(TrackerInterface tracker, String id, String name, Map<String, Float> stats) {
        this.tracker = tracker;

        this.id = id;
        this.name = name;
        this.stats = stats;
        this.tracking = true;
        this.sendMessages = true;
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
     * Returns a map of all the stats
     *
     * Key: the stat type
     * Value: the stat value
     *
     * @return a map of all the stats
     */
    public Map<String, Float> getStats() {
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
     * Returns the name of the record
     *
     * @return the name of the record
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the record
     *
     * @param name the name of the record
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the rating of the record
     *
     * @return the rating of the record
     */
    public float getRating() {
        return stats.get(StatType.RATING.getInternalName());
    }

    /**
     * Sets the rating of the record
     *
     * @param rating the rating of the record
     */
    public void setRating(float rating) {
        stats.put(StatType.RATING.getInternalName(), rating);
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

    /**
     * Returns if tracker messages should be sent for this record
     *
     * @return if tracker messages should be sent for this record
     */
    public boolean isSendingMessages() {
        return sendMessages;
    }

    /**
     * Sets if tracker messages should be sent for this record
     *
     * @param sendMessages if tracker messages should be sent for this record
     */
    public void setSendMessages(boolean sendMessages) {
        this.sendMessages = sendMessages;
    }
}
