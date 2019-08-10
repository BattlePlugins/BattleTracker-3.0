package org.battleplugins.tracker.stat.record;

import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and holds 'dummy' tracker data. None of this is
 * or should be stored into a database.
 *
 * @author Redned
 */
public class DummyRecord extends Record {

    public DummyRecord(TrackerInterface tracker, String id) {
        this(tracker, id, new HashMap<>(), BattleTracker.getInstance().getDefaultCalculator().getDefaultRating());
    }

    public DummyRecord(TrackerInterface tracker, String id, float rating) {
        this(tracker, id, new HashMap<>(), rating);
    }

    public DummyRecord(TrackerInterface tracker, String id, Map<String, Integer> stats, float rating) {
        super(tracker, id, stats, rating);
    }
}
