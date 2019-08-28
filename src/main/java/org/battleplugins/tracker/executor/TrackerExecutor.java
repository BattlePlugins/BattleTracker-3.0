package org.battleplugins.tracker.executor;

import mc.alk.battlecore.executor.CustomCommandExecutor;
import mc.alk.mc.ChatColor;
import mc.alk.mc.command.MCCommandSender;
import org.battleplugins.tracker.BattleTracker;
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
        sender.sendMessage(ChatColor.RED + "==== " + ChatColor.YELLOW + interfaceName + " Leaderboards" + ChatColor.RED + " ====");
        Map<UUID, Record> records = tracker.getTrackerManager().getInterface(interfaceName).getRecords();
        Map<Integer, Record> unsortedRecords = new HashMap<>();
        records.forEach((name, record) -> unsortedRecords.put((int) record.getRating(), record));

        LinkedHashMap<Integer, Record> sortedRecords = new LinkedHashMap<>();
        unsortedRecords.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder())).forEachOrdered(x -> sortedRecords.put(x.getKey(), x.getValue()));

        int i = 1;
        for (Map.Entry<Integer, Record> recordEntry : sortedRecords.entrySet()) {
            sender.sendMessage(ChatColor.GOLD + "#" + i + " " + ChatColor.YELLOW + recordEntry.getValue().getName() + " - "
                    + ChatColor.AQUA + (int) recordEntry.getValue().getRating() + " "
                    + ChatColor.RED + "Kills: " + ChatColor.GOLD + (int) recordEntry.getValue().getStat(StatType.KILLS)
                    + ChatColor.RED + " Deaths: " + ChatColor.GOLD + (int) recordEntry.getValue().getStat(StatType.DEATHS));
            i++;
        }
    }
}
