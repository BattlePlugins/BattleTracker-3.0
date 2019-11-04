package org.battleplugins.tracker.util;

import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.StatTypes;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.tracking.stat.tally.VersusTally;

import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.chat.MessageBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds utility methods for BattleTracker.
 *
 * @author Redned
 */
public class TrackerUtil {

    /**
     * Returns the formatted entity name from the given string
     *
     * @param name the formatted name
     * @param capitalizeFirst if the first letter of each word should be capitalized
     * @return the formatted entity name
     */
    public static String getFormattedEntityName(String name, boolean capitalizeFirst) {
        switch (name) {
            case "EVOCATION_FANGS":
                name = "EVOKER_FANGS";
                break;
            case "EVOCATION_ILLAGER":
                name = "EVOKER";
                break;
            case "ILLUSION_ILLAGER":
                name = "ILLUSIONER";
                break;
            case "PIG_ZOMBIE":
                name = "ZOMBIE_PIGMAN";
                break;
            case "VINDICATION_ILLAGER":
                name = "VINDICATOR";
                break;
        }
        name = name.toLowerCase();
        name = name.replace(" ", "_");
        if (capitalizeFirst)
            name = capitalizeFirst(name);

        return name;
    }

    /**
     * Capitalizes the first letter of each word
     * in a string
     *
     * @param string the string to capitalizeFirst
     * @return a string with the first letter of every word capitalized
     */
    public static String capitalizeFirst(String string) {
        String[] words = string.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }


        return String.join(" ", words);
    }

    /**
     * Does a variable replacement of the specified text
     * with the given map containing the replacements
     *
     * Key: the placeholder to replace
     * Value: the value to replace the placeholder with
     *
     * @param str the string to replace text on
     * @param replacements the replacements
     * @return the replaced message
     */
    public static String replacePlaceholders(String str, Map<String, String> replacements) {
        for (Map.Entry<String, String> replaceEntry : replacements.entrySet()) {
            str = str.replace(replaceEntry.getKey(), replaceEntry.getValue());
        }
        return str;
    }

    /**
     * Does a variable replacement for all values
     * stored inside of record stats.
     *
     * @param str the string to replace text on
     * @param record the record to use
     * @return the replaced message
     */
    public static String replaceRecordValues(String str, Record record) {
        for (Map.Entry<String, Float> recordEntry : record.getStats().entrySet()) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("%" + recordEntry.getKey().toLowerCase() + "%", String.valueOf(recordEntry.getValue()));
            replacePlaceholders(str, replacements);
        }

        return str;
    }

    /**
     * Returns a map of sorted records sorted by the rating
     *
     * Key: the Record
     * Value: the rating
     *
     * @param trackerInterface the tracker interface
     * @param limit a limit of how many elements can be in the map
     * @return a map of sorted records sorted by the rating
     */
    public static Map<Record, Float> getSortedRecords(TrackerInterface trackerInterface, int limit) {
        Map<UUID, Record> records = trackerInterface.getRecords();
        Map<Record, Float> unsortedRecords = new HashMap<>();

        for (Map.Entry<UUID, Record> record : records.entrySet()) {
            unsortedRecords.put(record.getValue(), record.getValue().getRating());
        }

        Map<Record, Float> sortedRecords = new LinkedHashMap<>();
        unsortedRecords.entrySet().stream().limit(limit).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> sortedRecords.put(x.getKey(), x.getValue()));
        return sortedRecords;
    }

    /**
     * Updates the PvP stats for the given players
     *
     * @param killed the player killed
     * @param killer the killer
     */
    public static void updatePvPStats(MCOfflinePlayer killed, MCOfflinePlayer killer) {
        BattleTracker tracker = BattleTracker.getInstance();
        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();

        Record killerRecord = pvpTracker.getOrCreateRecord(killer);
        Record killedRecord = pvpTracker.getOrCreateRecord(killed);

        if (killerRecord.isTracking())
            pvpTracker.incrementValue(StatTypes.KILLS, killer);

        if (killedRecord.isTracking())
            pvpTracker.incrementValue(StatTypes.DEATHS, killed);

        pvpTracker.updateRating(tracker.getPlatform().getOfflinePlayer(killer.getUniqueId()), tracker.getPlatform().getOfflinePlayer(killed.getUniqueId()), false);

        if (killerRecord.getStat(StatTypes.STREAK) % tracker.getConfig().getInt("streakMessageEvery", 15) == 0) {
            String streakMessage = tracker.getMessageManager().getFormattedStreakMessage(tracker.getPlatform().getOfflinePlayer(killer.getUniqueId()), String.valueOf((int) killerRecord.getStat(StatTypes.STREAK)));
            MCPlatform.broadcastMessage(MessageBuilder.builder().setMessage(streakMessage).build());
        }

        VersusTally versusTally = pvpTracker.getOrCreateVersusTally(killer, killed);

        // The format is killer : killed : stat1 : stat2 ....
        // If the killer is in place of the killed, we need to swap the values
        boolean addToKills = true;
        if (versusTally.getId2().equals(killer.getUniqueId().toString()))
            addToKills = false;

        if (addToKills) {
            versusTally.getStats().put(StatTypes.KILLS.getInternalName(), versusTally.getStats().getOrDefault(StatTypes.KILLS.getInternalName(), 0f) + 1);
        } else {
            versusTally.getStats().put(StatTypes.DEATHS.getInternalName(), versusTally.getStats().getOrDefault(StatTypes.DEATHS.getInternalName(), 0f) + 1);
        }
    }
}
