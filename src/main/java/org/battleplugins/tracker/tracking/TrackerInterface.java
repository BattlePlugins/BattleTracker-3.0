package org.battleplugins.tracker.tracking;

import mc.alk.mc.MCOfflinePlayer;
import org.battleplugins.tracker.tracking.message.DeathMessageManager;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.tracking.stat.record.Record;

import java.util.Map;
import java.util.UUID;

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
     * Returns if a player has a record in the tracker
     *
     * @param player the player to check
     * @return if a player has a record in the tracker
     */
    boolean hasRecord(MCOfflinePlayer player);

    /**
     * Returns the record for the given OfflinePlayer
     *
     * @param player the OfflinePlayer to get the record from
     * @return the record for the given OfflinePlayer
     */
    Record getRecord(MCOfflinePlayer player);

    /**
     * Returns a map of all the records
     *
     * Key: the UUID of the player
     * Value: the record value
     *
     * @return a map of all the records
     */
    Map<UUID, Record> getRecords();

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
    void setValue(StatType statType, float value, MCOfflinePlayer player);

    /**
     * Sets a value with the given stat type
     *
     * @param statType the stat type (String) to set the value for
     * @param value the value to set
     * @param player the player to set the value for
     */
    void setValue(String statType, float value, MCOfflinePlayer player);

    /**
     * Sets the ranking for the specified players
     *
     * @param killer the player to increment the rating for
     * @param loser the player to decrement the rating for
     * @param tie if the end result was a tie
     */
    void updateRating(MCOfflinePlayer killer, MCOfflinePlayer loser, boolean tie);

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
     * Adds a record for the specified player to the tracker from the
     * default SQL columns.
     *
     * @param player the player to create the record for
     */
    void createNewRecord(MCOfflinePlayer player);

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
     * Returns the death message manager for this tracker interface
     *
     * @return the death message manager for this tracker interface
     */
    DeathMessageManager getDeathMessageManager();

    /**
     * Returns the recap manager for this tracker interface
     *
     * @return the recap manager for this tracker interface
     */
    RecapManager getRecapManager();

    /**
     * Returns the rating calculator for this tracker interface
     *
     * @return the rating calculator for this tracker interface
     */
    RatingCalculator getRatingCalculator();

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
