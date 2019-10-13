package org.battleplugins.tracker.tracking.recap;

/**
 * Stores more information about a specific damage.
 *
 * @author Redned
 */
public class DamageInfo {

    private String cause;
    private double damage;
    private long logTime;

    public DamageInfo(String cause, double damage) {
        this.cause = cause;
        this.damage = damage;
        this.logTime = System.currentTimeMillis();
    }

    /**
     * Returns the cause of this damage
     *
     * @return the cause of this damage
     */
    public String getCause() {
        return cause;
    }

    /**
     * Returns the amount of damage dealt
     *
     * @return the amount of damage dealt
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Returns when this damage info was logged
     *
     * @return when this damage info was logged
     */
    public long getLogTime() {
        return logTime;
    }
}
