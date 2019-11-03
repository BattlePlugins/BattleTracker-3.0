package org.battleplugins.tracker.util;

import mc.alk.mc.ChatColor;
import mc.alk.mc.MCWorld;
import mc.alk.mc.block.MCSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.sign.SignManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sign utility class for BattleTracker.
 *
 * @author Redned
 */
public class SignUtil {

    /**
     * Checks if a sign can be a leaderboard sign; generally
     * checked upon placing a sign
     *
     * @param lines the lines to check in
     * @return if the sign can be a leaderboard sign
     */
    public static boolean isLeaderboardSign(String[] lines) {
        Collection<TrackerInterface> trackers = BattleTracker.getInstance().getTrackerManager().getInterfaces().values();
        TrackerInterface tracker = null;
        String statType = null;
        for (TrackerInterface loopedTracker : trackers) {
            for (String line : lines) {
                if (!ChatColor.stripColor(line).toLowerCase().contains(loopedTracker.getName().toLowerCase()))
                    continue;

                tracker = loopedTracker;
                break;
            }
        }

        if (tracker == null)
            return false;

        for (String line : lines) {
            for (String loopedStatType : tracker.getRecords().entrySet()
                    .iterator().next().getValue().getStats().keySet()) {
                if (!ChatColor.stripColor(line).toLowerCase().contains(loopedStatType.toLowerCase()))
                    continue;

                statType = loopedStatType;
            }
        }

        if (statType == null)
            return false;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("%tracker%", tracker.getName());
        replacements.put("%stat%", statType);

        SignManager signManager = BattleTracker.getInstance().getSignManager();
        for (int i = 0; i < signManager.getLeaderboardFormat().length; i++) {
            String line = signManager.getLeaderboardFormat()[i];
            String formattedLine = TrackerUtil.replacePlaceholders(line, replacements);

            // One line is enough
            if (ChatColor.stripColor(formattedLine).equalsIgnoreCase(ChatColor.stripColor(lines[i])))
                return true;
        }

        return false;
    }

    /**
     * Returns the name of the tracker from the
     * given sign lines
     *
     * @param lines the lines of the sign
     * @return the name of the tracker
     */
    public static String getTrackerName(String[] lines) {
        Collection<TrackerInterface> trackers = BattleTracker.getInstance().getTrackerManager().getInterfaces().values();
        for (TrackerInterface loopedTracker : trackers) {
            for (String line : lines) {
                if (!ChatColor.stripColor(line).toLowerCase().contains(loopedTracker.getName().toLowerCase()))
                    continue;

                return loopedTracker.getName();
            }
        }

        return "";
    }

    /**
     * Returns the name of the stat type from the
     * given sign lines
     *
     * @param lines the lines of the sign
     * @return the name of the stat
     */
    public static String getStatType(String[] lines) {
        String trackerName = getTrackerName(lines);
        if (trackerName == null || trackerName.isEmpty())
            return "";

        TrackerInterface tracker = BattleTracker.getInstance().getTrackerManager().getInterface(trackerName);
        for (String line : lines) {
            for (String loopedStatType : tracker.getRecords().entrySet()
                    .iterator().next().getValue().getStats().keySet()) {
                if (!ChatColor.stripColor(line).toLowerCase().contains(loopedStatType.toLowerCase()))
                    continue;

                return loopedStatType;
            }
        }

        return "";
    }

    /**
     * Returns all the signs below the given leaderboard sign
     *
     * @param sign the leaderboard sign to get the signs below
     * @return all the signs below the given leaderboard sign
     */
    public static List<MCSign> getSignsBelow(LeaderboardSign sign) {
        List<MCSign> signs = new ArrayList<>();
        MCWorld world = sign.getLocation().getWorld();
        int x = sign.getLocation().getBlockX();
        int y = sign.getLocation().getBlockY();
        int z = sign.getLocation().getBlockZ();
        while (world.isType(world.getBlockAt(x, y--, z), MCSign.class)) {
            signs.add(world.toType(world.getBlockAt(x, y, z), MCSign.class));
        }
        return signs;
    }
}
