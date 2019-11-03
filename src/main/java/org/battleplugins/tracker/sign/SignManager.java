package org.battleplugins.tracker.sign;

import lombok.AccessLevel;
import lombok.Getter;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.configuration.ConfigurationSection;
import mc.alk.battlecore.controllers.MessageController;
import mc.alk.battlecore.util.LocationUtil;
import mc.alk.battlecore.util.Log;
import mc.alk.mc.MCLocation;
import mc.alk.mc.block.MCBlock;
import mc.alk.mc.block.MCSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.SignUtil;
import org.battleplugins.tracker.util.TrackerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for signs in BattleTracker.
 *
 * @author Redned
 */
@Getter
public class SignManager {

    @Getter(AccessLevel.NONE)
    private BattleTracker tracker;

    /**
     * If signs below should be updated
     *
     * @return if signs below should be updated
     */
    private boolean updateSignsBelow;

    /**
     * If personal signs are enabled
     *
     * @return if personal signs are enabled
     */
    private boolean personalSignsEnabled;

    /**
     * The listing format for leaderboard signs
     *
     * @return the listing format for leaderboard signs
     */
    private String listingFormat;

    /**
     * The sign format for leaderboard signs
     *
     * @return the sign format for leaderboard signs
     */
    private String[] leaderboardFormat;

    /**
     * The sign format for personal signs
     *
     * @return the sign format for personal signs
     */
    private String[] personalFormat;

    /**
     * A map of all the signs
     *
     * Key: the location of the sign
     * Value: the tracker sign
     *
     * @return a map of all the signs
     */
    private Map<MCLocation, LeaderboardSign> signs;

    public SignManager(BattleTracker tracker) {
        this.tracker = tracker;
        this.signs = new HashMap<>();

        loadDataFromConfig("signs", tracker.getConfigManager().getSignsConfig());
        loadDataFromSaves("signs", tracker.getConfigManager().getSignSaves());
    }

    /**
     * Refreshes the content of a sign
     *
     * @param sign the sign to refresh the content on
     */
    public void refreshSignContent(MCSign sign) {
        if (!signs.containsKey(sign.getLocation())) {
            Log.debug("Sign at location " + sign.getLocation() + " is not a tracker sign!");
            return;
        }

        String trackerName = SignUtil.getTrackerName(sign.getLines());
        String statType = SignUtil.getStatType(sign.getLines());

        Map<String, String> replacements = new HashMap<>();
        replacements.put("%tracker%", trackerName);
        replacements.put("%stat%", statType);

        int normalSignLines = 0;
        int extraLines = 0;
        String[] lines = leaderboardFormat;
        for (int i = 0; i < lines.length; i++) {
            lines[i] = TrackerUtil.replacePlaceholders(lines[i], replacements);
            sign.setLine(i, lines[i]);
            if (lines[i].contains("%leaderboard_format%"))
                normalSignLines += 1;
        }

        List<MCSign> signsBelow = SignUtil.getSignsBelow(signs.get(sign.getLocation()));
        extraLines = signsBelow.size() * 4;

        Map<Record, Float> recordsMap = TrackerUtil.getSortedRecords(tracker.getTrackerManager().getInterface(trackerName), extraLines + normalSignLines);
        if (recordsMap.isEmpty())
            return;

        List<Record> records = new ArrayList<>(recordsMap.keySet());
        int recordIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (records.size() <= recordIndex) {
                if (line.contains("%listing_format%"))
                    sign.setLine(i, "");

                continue;
            }

            if (!line.contains("%listing_format%"))
                continue;

            line = line.replace("%listing_format%", listingFormat);
            String formattedLine = line.replace("%ranking%", String.valueOf(recordIndex + 1));
            formattedLine = formattedLine.replace("%player_name%", records.get(recordIndex).getName());
            formattedLine = formattedLine.replace("%value%", String.valueOf(records.get(recordIndex).getStat(statType)));

            TrackerUtil.replaceRecordValues(formattedLine, records.get(recordIndex));
            sign.setLine(i, MessageController.colorChat(formattedLine));

            recordIndex++;
        }

        sign.update(true);

        if (!updateSignsBelow || signsBelow.isEmpty() || records.size() <= recordIndex)
            return;

        for (MCSign signBelow : signsBelow) {
            for (int i = 0; i < signBelow.getLines().length; i++) {
                String line = lines[i];
                String formattedLine = line.replace("%ranking%", String.valueOf(recordIndex + 1));

                TrackerUtil.replaceRecordValues(formattedLine, records.get(recordIndex));
                sign.setLine(i, MessageController.colorChat(formattedLine));
            }

            signBelow.update(true);
        }
    }

    /**
     * Adds a sign to the leaderboard signs
     *
     * @param sign the leaderboard sign to add
     */
    public void addSign(LeaderboardSign sign) {
        signs.put(sign.getLocation(), sign);
    }

    /**
     * Saves all the signs to the specified config file
     *
     * @param path the path to save the signs in
     * @param config the config (saves) file to save them in
     */
    public void saveSigns(String path, Configuration config) {
        int index = 1;
        for (Map.Entry<MCLocation, LeaderboardSign> signEntry : this.signs.entrySet()) {
            String signPath = path + "." + index;
            config.set(signPath + ".location", LocationUtil.toString(signEntry.getKey()));
            config.set(signPath + ".tracker", signEntry.getValue().getTrackerName());
            config.set(signPath + ".statType", signEntry.getValue().getStatType());
        }

        config.save();
    }

    private void loadDataFromConfig(String path, Configuration config) {
        ConfigurationSection leaderboardSection = config.getSection(path + ".leaderboard");
        ConfigurationSection personalSection = config.getSection(path + ".personal");

        this.leaderboardFormat = new String[4];
        this.leaderboardFormat[0] = MessageController.colorChat(leaderboardSection.getString("lines.1", ""));
        this.leaderboardFormat[1] = MessageController.colorChat(leaderboardSection.getString("lines.2", ""));
        this.leaderboardFormat[2] = MessageController.colorChat(leaderboardSection.getString("lines.3", ""));
        this.leaderboardFormat[3] = MessageController.colorChat(leaderboardSection.getString("lines.4", ""));

        this.updateSignsBelow = leaderboardSection.getBoolean("updateSignsBelow", true);
        this.listingFormat = leaderboardSection.getString("listingFormat", "");

        this.personalFormat = new String[4];
        this.personalFormat[0] = MessageController.colorChat(personalSection.getString("lines.1"));
        this.personalFormat[1] = MessageController.colorChat(personalSection.getString("lines.2"));
        this.personalFormat[2] = MessageController.colorChat(personalSection.getString("lines.3"));
        this.personalFormat[3] = MessageController.colorChat(personalSection.getString("lines.4"));

        this.personalSignsEnabled = personalSection.getBoolean("enabled", true);
    }

    private void loadDataFromSaves(String path, Configuration config) {
        for (String str : config.getSection(path).getKeys(false)) {
            ConfigurationSection section = config.getSection(path + "." + str);
            MCLocation location = LocationUtil.fromString(section.getString("location"));
            if (location == null) {
                Log.warn("Location " + section.getString("location") + " was invalid for sign " + str + ".");
                continue;
            }

            MCSign sign = getSign(location, true);
            if (sign == null)
                continue;

            String trackerName = section.getString("tracker");
            if (trackerName == null || trackerName.isEmpty()) {
                Log.warn("Sign " + str + " does not specify a tracker!");
                continue;
            }

            LeaderboardSign leaderboardSign = new LeaderboardSign(location, section.getString("statType", "rating"), trackerName);
            signs.put(location, leaderboardSign);
        }
    }

    private MCSign getSign(MCLocation location, boolean showWarning) {
        MCBlock block = location.getWorld().getBlockAt(location);
        if (!block.getWorld().isType(block, MCSign.class)) {
            if (showWarning)
                Log.warn("Block at location " + location.toString() + " is not a sign!");

            return null;
        }

        return block.getWorld().toType(block, MCSign.class);
    }
}
