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

    @MCCommand(cmds = "reset", perm = "battletracker.reset")
    public void resetCommand(MCCommandSender sender, MCOfflinePlayer player) {
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        trackerInterface.createNewRecord(player);

        sender.sendMessage(ChatColor.GREEN + "Reset " + player.getName() + "'s " + trackerInterface.getName() + " data!");
    }
}
