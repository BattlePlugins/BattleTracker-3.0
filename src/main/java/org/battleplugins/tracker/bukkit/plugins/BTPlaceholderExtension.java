package org.battleplugins.tracker.bukkit.plugins;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.Util;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BTPlaceholderExtension extends PlaceholderExpansion {

    private BattleTracker tracker;

    public BTPlaceholderExtension(BattleTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public String getIdentifier() {
        return "BT";
    }

    @Override
    public String getAuthor() {
        return "BattlePlugins";
    }

    @Override
    public String getVersion() {
        return tracker.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null || !player.isOnline())
            return "";

        String[] split = params.split("_");
        String interfaceName = split[0];

        // The interface is not tracked or does not exist
        if (!tracker.getTrackerManager().hasInterface(interfaceName))
            return "";

        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        Record record = trackerInterface.getRecord(tracker.getPlatform().getPlayer(player.getUniqueId()));

        // Gets leaderboard stats (ex: %bt_pvp_top_kills_1%)
        if (split[1].equalsIgnoreCase("top")) {
            try {
                Integer.parseInt(split[3]);
            } catch (NumberFormatException ex) {
                return null; // not a number at the end of the placeholder
            }

            String stat = split[2];
            int ranking = Integer.parseInt(split[3]);
            if (!record.getStats().containsKey(stat))
                return "";

            Map<Record, Float> sortedRecords = Util.getSortedRecords(trackerInterface, -1);
            List<Record> records = new ArrayList<>(sortedRecords.keySet());
            return String.valueOf(records.get(ranking).getStat(stat));
        }

        // Gets player stats (ex: %bt_pvp_kills%)
        if (record.getStats().containsKey(split[1]))
            return String.valueOf(record.getStat(split[1]));

        return null;
    }
}
