package org.battleplugins.tracker.stat.record;

import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and holds player tracker data before it
 * is put into the database.
 *
 * @author Redned
 */
public class PlayerRecord extends Record {

    public PlayerRecord(TrackerInterface tracker, String id) {
        this(tracker, id, new HashMap<>(), BattleTracker.getInstance().getDefaultCalculator().getDefaultRating());
    }

    public PlayerRecord(TrackerInterface tracker, String id, float rating) {
        this(tracker, id, new HashMap<>(), rating);
    }

    public PlayerRecord(TrackerInterface tracker, String id, Map<String, Integer> stats, float rating) {
        super(tracker, id, stats, rating);
    }
}
