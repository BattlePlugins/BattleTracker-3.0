package org.battleplugins.tracker.tracking.stat.calculator;

import org.battleplugins.tracker.tracking.stat.record.Record;

/**
 * Interface for rating calculators
 *
 * @author alkarin_v, Redned
 */
public interface RatingCalculator {

    /**
     * Returns the name of the calculator
     *
     * @return the name of the calculator
     */
    String getName();

    /**
     * Returns the default rating
     *
     * @return the default rating
     */
    float getDefaultRating();

    /**
     * Sets the default rating
     *
     * @param defaultRating the default rating
     */
    void setDefaultRating(float defaultRating);

    /**
     * Updates the rating of
     *
     * @param killer the killer's Record
     * @param killed the player killed's Record
     * @param tie if the final result is a tie
     */
    void updateRating(Record killer, Record killed, boolean tie);

    /**
     * Updates the rating of
     *
     * @param killer the killer's Record
     * @param killed an array of the players killed's Record
     * @param tie if the final result is a tie
     */
    void updateRating(Record killer, Record[] killed, boolean tie);
}
