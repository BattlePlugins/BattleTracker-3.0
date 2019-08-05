package org.battleplugins.tracker;

import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;

/**
 * Main interface used for tracking.
 *
 * @author Redned
 */
public interface TrackerInterface {

    /**
     * Returns the name of the tracker
     *
     * @return the name of the tracker
     */
    String getName();

    /**
     * Returns the amount of stored records for this tracker
     *
     * @return the amount of stored records
     */
    int getRecordCount();

    /**
     * Increments a value with the given stat type
     *
     * @param statType the stat type to increment the value for
     * @param player the player to increment the value for
     */
    void incrementValue(StatType statType, MCOfflinePlayer player);

    /**
     * Increments a value with the given stat type
     *
     * @param statType the stat type (String) to increment the value for
     * @param player the player to increment the value for
     */
    void incrementValue(String statType, MCOfflinePlayer player);

    /**
     * Decrements a value with the given stat type
     *
     * @param statType the stat type to decrement the value for
     * @param player the player to decrement the value for
     */
    void decrementValue(StatType statType, MCOfflinePlayer player);

    /**
     * Decrements a value with the given stat type
     *
     * @param statType the stat type (String) to decrement the value for
     * @param player the player to decrement the value for
     */
    void decrementValue(String statType, MCOfflinePlayer player);

    /**
     * Sets a value with the given stat type
     *
     * @param statType the stat type to set the value for
     * @param value the value to set
     * @param player the player to set the value for
     */
    void setValue(StatType statType, int value, MCOfflinePlayer player);

    /**
     * Sets a value with the given stat type
     *
     * @param statType the stat type (String) to set the value for
     * @param value the value to set
     * @param player the player to set the value for
     */
    void setValue(String statType, int value, MCOfflinePlayer player);

    /**
     * Sets the ranking for the specified players
     *
     * @param killer the player to increment the rating for
     * @param loser the player to decrement the rating for
     */
    void updateRating(MCOfflinePlayer killer, MCOfflinePlayer loser);

    /**
     * Enables tracking for the specified player
     *
     * @param player the player to enable tracking for
     */
    void enableTracking(MCOfflinePlayer player);

    /**
     * Disables tracking for the specified player
     *
     * @param player the player to disable tracking for
     */
    void disableTracking(MCOfflinePlayer player);

    /**
     * Enables messages for the specified player
     *
     * @param player the player to enable messages for
     */
    void enableMessages(MCOfflinePlayer player);

    /**
     * Disables messages for the specified player
     *
     * @param player the player to disable messages for
     */
    void disableMessages(MCOfflinePlayer player);

    /**
     * Adds a record for the specified player to the tracker
     *
     * @param player the player to create the record for
     * @param record the record to add
     */
    void createNewRecord(MCOfflinePlayer player, Record record);

    /**
     * Removes the record for the specified player
     *
     * @param player the player to remove the record for
     */
    void removeRecord(MCOfflinePlayer player);

    /**
     * Immediately save all records and empty the cache
     */
    void flush();

    /**
     * Saves the records to the database for the specified player
     *
     * @param player the player to save records for
     */
    void save(MCOfflinePlayer player);

    /**
     * Saves the records for all the players in the cache
     */
    void saveAll();
}
