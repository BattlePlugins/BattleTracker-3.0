package org.battleplugins.tracker.stat.record;

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

    public PlayerRecord(TrackerInterface tracker, String id, String name) {
        this(tracker, id, name, new HashMap<>());
    }

    public PlayerRecord(TrackerInterface tracker, String id, String name, Map<String, Float> stats) {
        super(tracker, id, name, stats);
    }
}
