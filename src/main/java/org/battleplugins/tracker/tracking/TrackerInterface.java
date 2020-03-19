package org.battleplugins.tracker.tracking;

import org.battleplugins.api.entity.living.player.OfflinePlayer;
import org.battleplugins.tracker.tracking.message.DeathMessageManager;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.tracking.stat.tally.VersusTally;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    boolean hasRecord(OfflinePlayer player);

    /**
     * Returns the record for the given OfflinePlayer
     *
     * @param player the OfflinePlayer to get the record from
     * @return the record for the given OfflinePlayer
     */
    Optional<Record> getRecord(OfflinePlayer player);

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
     * Returns if a player has a versus tally in the tracker
     *
     * @param player the player to check
     * @return if a player has a versus tally in the tracker
     */
    boolean hasVersusTally(OfflinePlayer player);

    /**
     * Returns the versus tally for the given OfflinePlayers
     *
     * @param player1 the first OfflinePlayer to get the versus tally from
     * @param player2 the second OfflinePlayer to get the versus tally from
     * @return the versus tally for the given OfflinePlayers
     */
    Optional<VersusTally> getVersusTally(OfflinePlayer player1, OfflinePlayer player2);

    /**
     * Creates a new versus tally for the given OfflinePlayers
     *
     * @param player1 the first OfflinePlayer in the versus tally
     * @param player2 the second OfflinePlayer in the versus tally
     * @return the new versus tally for the given OfflinePlayers
     */
    VersusTally createNewVersusTally(OfflinePlayer player1, OfflinePlayer player2);

    /**
     * Returns or creates a versus tally for the given OfflinePlayers
     *
     * @param player1 the first OfflinePlayer to get the versus tally from
     * @param player2 the second OfflinePlayer to get the versus tally from
     * @return the versus tally for the given OfflinePlayers or create a new one
     */
    default VersusTally getOrCreateVersusTally(OfflinePlayer player1, OfflinePlayer player2) {
        return getVersusTally(player1, player2).orElse(createNewVersusTally(player1, player2));
    }
    /**
     * Returns a list of all the versus tallies
     *
     * @return a list of all the versus tallies
     */
    List<VersusTally> getVersusTallies();

    /**
     * Increments a value with the given stat type
     *
     * @param statType the stat type to increment the value for
     * @param player the player to increment the value for
     */
    default void incrementValue(StatType statType, OfflinePlayer player) {
        incrementValue(statType.getInternalName(), player);
    }

    /**
     * Increments a value with the given stat type
     *
     * @param statType the stat type (String) to increment the value for
     * @param player the player to increment the value for
     */
    default void incrementValue(String statType, OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setValue(statType, opRecord.get().getStat(statType) + 1);
    }

    /**
     * Decrements a value with the given stat type
     *
     * @param statType the stat type to decrement the value for
     * @param player the player to decrement the value for
     */
    default void decrementValue(StatType statType, OfflinePlayer player) {
        decrementValue(statType.getInternalName(), player);
    }

    /**
     * Decrements a value with the given stat type
     *
     * @param statType the stat type (String) to decrement the value for
     * @param player the player to decrement the value for
     */
    default void decrementValue(String statType, OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setValue(statType, opRecord.get().getStat(statType) - 1);
    }

    /**
     * Sets a value with the given stat type
     *
     * @param statType the stat type to set the value for
     * @param value the value to set
     * @param player the player to set the value for
     */
    default void setValue(StatType statType, float value, OfflinePlayer player) {
        setValue(statType.getInternalName(), value, player);
    }

    /**
     * Sets a value with the given stat type
     *
     * @param statType the stat type (String) to set the value for
     * @param value the value to set
     * @param player the player to set the value for
     */
    void setValue(String statType, float value, OfflinePlayer player);

    /**
     * Sets the ranking for the specified players
     *
     * @param killer the player to increment the rating for
     * @param loser the player to decrement the rating for
     * @param tie if the end result was a tie
     */
    void updateRating(OfflinePlayer killer, OfflinePlayer loser, boolean tie);

    /**
     * Enables tracking for the specified player
     *
     * @param player the player to enable tracking for
     */
    default void enableTracking(OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setTracking(true);
    }

    /**
     * Disables tracking for the specified player
     *
     * @param player the player to disable tracking for
     */
    default void disableTracking(OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setTracking(false);
    }

    /**
     * Enables messages for the specified player
     *
     * @param player the player to enable messages for
     */
    default void enableMessages(OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setSendingMessages(true);
    }

    /**
     * Disables messages for the specified player
     *
     * @param player the player to disable messages for
     */
    default void disableMessages(OfflinePlayer player) {
        Optional<Record> opRecord = getRecord(player);
        if (!opRecord.isPresent())
            return;

        opRecord.get().setSendingMessages(false);
    }

    /**
     * Adds a record for the specified player to the tracker from the
     * default SQL columns.
     *
     * @param player the player to create the record for
     * @return the new record created
     */
    Record createNewRecord(OfflinePlayer player);

    /**
     * Adds a record for the specified player to the tracker
     *
     * @param player the player to create the record for
     * @param record the record to add
     * @return the new record created
     */
    Record createNewRecord(OfflinePlayer player, Record record);

    /**
     * Returns the record for the given player or creates
     * a new one of one was unable to be found
     *
     * @param player the player to get/create the record for
     * @return the record for the given player or create a new one
     */
    default Record getOrCreateRecord(OfflinePlayer player) {
        return getRecord(player).orElse(createNewRecord(player));
    }

    /**
     * Removes the record for the specified player
     *
     * @param player the player to remove the record for
     */
    void removeRecord(OfflinePlayer player);

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
    void save(OfflinePlayer player);

    /**
     * Saves the records for all the players in the cache
     */
    void saveAll();
}
