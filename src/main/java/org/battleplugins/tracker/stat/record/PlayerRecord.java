package org.battleplugins.tracker.stat.record;

import org.battleplugins.tracker.TrackerInterface;

import java.util.Map;

/**
 * Stores and holds player tracker data before it
 * is put into the database.
 *
 * @author Redned
 */
public class PlayerRecord extends Record {

    public PlayerRecord(TrackerInterface tracker, String id, Map<String, Integer> stats, int rating) {
        super(tracker, id, stats, rating);
    }
}
