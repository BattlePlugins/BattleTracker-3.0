package org.battleplugins.tracker;

/**
 * Class that holds information about BattleTracker
 * from the pom. Due to how the Sponge API is written,
 * this class had to be created.
 *
 * Values here are replaced at runtime.
 *
 * @author Redned
 */
public class TrackerInfo {

    public static final String NAME = "${project.name}";
    public static final String VERSION = "${project.version}";
    public static final String DESCRIPTION = "${project.description}";
    public static final String URL = "${project.url}";
}