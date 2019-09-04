package org.battleplugins.tracker.executor;

import mc.alk.battlecore.executor.CustomCommandExecutor;
import mc.alk.mc.ChatColor;
import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.command.MCCommandSender;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main executor for trackers.
 *
 * @author Redned
 */
// TODO: Make all messages in here configurable options
public class TrackerExecutor extends CustomCommandExecutor {

    private BattleTracker tracker;
    private String interfaceName;

    public TrackerExecutor(BattleTracker tracker, String interfaceName) {
        this.tracker = tracker;
        this.interfaceName = interfaceName;
    }

    @MCCommand(cmds = "top")
    public void topCommand(MCCommandSender sender) {
        topCommand(sender, 5);
    }

    @MCCommand(cmds = "top")
    public void topCommand(MCCommandSender sender, int amount) {
        sender.sendMessage(ChatColor.RED + "==== " + ChatColor.YELLOW + interfaceName + " Leaderboards" + ChatColor.RED + " ====");
        Map<UUID, Record> records = tracker.getTrackerManager().getInterface(interfaceName).getRecords();
        Map<Record, Float> unsortedRecords = new HashMap<>();
        records.forEach((uuid, record) -> unsortedRecords.put(record, record.getRating()));

        Map<Record, Float> sortedRecords = new LinkedHashMap<>();
        unsortedRecords.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> sortedRecords.put(x.getKey(), x.getValue()));

        int i = 1;
        for (Map.Entry<Record, Float> recordEntry : sortedRecords.entrySet()) {
            sender.sendMessage(ChatColor.GOLD + "#" + i + " " + ChatColor.YELLOW + recordEntry.getKey().getName() + " - "
                    + ChatColor.AQUA + (int) recordEntry.getKey().getRating() + " "
                    + ChatColor.RED + "Kills: " + ChatColor.GOLD + (int) recordEntry.getKey().getStat(StatType.KILLS)
                    + ChatColor.RED + " Deaths: " + ChatColor.GOLD + (int) recordEntry.getKey().getStat(StatType.DEATHS));

            // limit at 100 to prevent lag and spam
            if (i >= amount || i >= 100)
                break;

            i++;
        }
    }

    @MCCommand(cmds = "rank")
    public void rankCommand(MCCommandSender sender, MCOfflinePlayer player) {
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        Record record = trackerInterface.getRecord(player);
        if (record == null) {
            sender.sendMessage(ChatColor.RED + "This player has never joined the server before!");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + player.getName() + " - " + ChatColor.AQUA + (int) record.getRating()
                + ChatColor.YELLOW + " (Max Rating: " + (int) record.getStat(StatType.MAX_RATING) + ") " + ChatColor.RED + "Kills: "
                + ChatColor.GOLD + (int) record.getStat(StatType.KILLS) + ChatColor.RED + " Deaths: " + ChatColor.GOLD + (int) record.getStat(StatType.DEATHS)
                + ChatColor.RED + " KDR: " + ChatColor.YELLOW + (record.getStat(StatType.KILLS) / record.getStat(StatType.DEATHS)));
    }

    @MCCommand(cmds = "reset", perm = "battletracker.reset")
    public void resetCommand(MCCommandSender sender, MCOfflinePlayer player) {
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        if (!trackerInterface.hasRecord(player)) {
            sender.sendMessage(ChatColor.RED + "This player has never joined the server before!");
            return;
        }

        trackerInterface.createNewRecord(player);

        sender.sendMessage(ChatColor.GREEN + "Reset " + player.getName() + "'s " + trackerInterface.getName() + " data!");
    }

    @MCCommand(cmds = "set", perm = "battletracker.set")
    public void setCommand(MCCommandSender sender, MCOfflinePlayer player, String statType, float value) {
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        Record record = trackerInterface.getRecord(player);
        if (record == null) {
            sender.sendMessage(ChatColor.RED + "This player has never joined the server before!");
            return;
        }

        if (!record.getStats().containsKey(statType.toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "This stat type does not exist in this tracker!");
            return;
        }

        trackerInterface.setValue(statType, value, player);
        sender.sendMessage(ChatColor.GREEN + "Set " + player.getName() + "'s " + statType.toLowerCase() + " value to " + value + "!");
    }
}
