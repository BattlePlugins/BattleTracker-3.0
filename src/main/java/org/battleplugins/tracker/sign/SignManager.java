package org.battleplugins.tracker.sign;

import lombok.AccessLevel;
import lombok.Getter;

import mc.alk.battlecore.message.MessageController;
import mc.alk.battlecore.util.LocationUtil;
import mc.alk.battlecore.util.Log;

import org.battleplugins.api.configuration.Configuration;
import org.battleplugins.api.configuration.ConfigurationNode;
import org.battleplugins.api.world.Location;
import org.battleplugins.api.world.block.Block;
import org.battleplugins.api.world.block.entity.BlockEntity;
import org.battleplugins.api.world.block.entity.Sign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.SignUtil;
import org.battleplugins.tracker.util.TrackerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manager for signs in BattleTracker.
 *
 * @author Redned
 */
@Getter
public class SignManager {

    @Getter(AccessLevel.NONE)
    private BattleTracker plugin;

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
    private Map<Location, LeaderboardSign> signs;

    public SignManager(BattleTracker plugin) {
        this.plugin = plugin;
        this.signs = new HashMap<>();

        loadDataFromConfig("signs", plugin.getConfigManager().getSignsConfig());
        loadDataFromSaves("signs", plugin.getConfigManager().getSignSaves());
    }

    /**
     * Refreshes the content of a sign
     *
     * @param sign the sign to refresh the content on
     */
    public void refreshSignContent(Sign sign) {
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

        List<Sign> signsBelow = SignUtil.getSignsBelow(signs.get(sign.getLocation()));
        extraLines = signsBelow.size() * 4;

        Optional<TrackerInterface> opTracker = plugin.getTrackerManager().getInterface(trackerName);
        if (!opTracker.isPresent())  {
            Log.debug("A tracker could not be found for " + trackerName + "!");
            return;
        }

        Map<Record, Float> recordsMap = TrackerUtil.getSortedRecords(opTracker.get(), extraLines + normalSignLines);
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

        for (Sign signBelow : signsBelow) {
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
        // TODO: Sign saving
        int index = 1;
        for (Map.Entry<Location, LeaderboardSign> signEntry : this.signs.entrySet()) {
            String signPath = path + "." + index;
            // config.set(signPath + ".location", LocationUtil.toString(signEntry.getKey()));
            // config.set(signPath + ".tracker", signEntry.getValue().getTrackerName());
            // config.set(signPath + ".statType", signEntry.getValue().getStatType());
        }

        // config.save();
    }

    private void loadDataFromConfig(String path, Configuration config) {
        ConfigurationNode leaderboardSection = config.getNode(path + ".leaderboard");
        ConfigurationNode personalSection = config.getNode(path + ".personal");

        this.leaderboardFormat = new String[4];
        this.leaderboardFormat[0] = MessageController.colorChat(leaderboardSection.getNode("lines.1").getValue(""));
        this.leaderboardFormat[1] = MessageController.colorChat(leaderboardSection.getNode("lines.2").getValue(""));
        this.leaderboardFormat[2] = MessageController.colorChat(leaderboardSection.getNode("lines.3").getValue(""));
        this.leaderboardFormat[3] = MessageController.colorChat(leaderboardSection.getNode("lines.4").getValue(""));

        this.updateSignsBelow = leaderboardSection.getNode("updateSignsBelow").getValue(true);
        this.listingFormat = leaderboardSection.getNode("listingFormat").getValue("");

        this.personalFormat = new String[4];
        this.personalFormat[0] = MessageController.colorChat(personalSection.getNode("lines.1").getValue(String.class));
        this.personalFormat[1] = MessageController.colorChat(personalSection.getNode("lines.2").getValue(String.class));
        this.personalFormat[2] = MessageController.colorChat(personalSection.getNode("lines.3").getValue(String.class));
        this.personalFormat[3] = MessageController.colorChat(personalSection.getNode("lines.4").getValue(String.class));

        this.personalSignsEnabled = personalSection.getNode("enabled").getValue(true);
    }

    private void loadDataFromSaves(String path, Configuration config) {
        for (String str : config.getNode(path).getCollectionValue(String.class)) {
            ConfigurationNode section = config.getNode(path + "." + str);
            Location location = LocationUtil.fromString(section.getNode("location").getValue(String.class));
            if (location == null) {
                Log.warn("Location " + section.getNode("location").getValue() + " was invalid for sign " + str + ".");
                continue;
            }

            if (!getSign(location, true).isPresent()) {
                continue;
            }

            Sign sign = getSign(location, true).get();
            String trackerName = section.getNode("tracker").getValue(String.class);
            if (trackerName == null || trackerName.isEmpty()) {
                Log.warn("Sign " + str + " does not specify a tracker!");
                continue;
            }

            LeaderboardSign leaderboardSign = new LeaderboardSign(location, section.getNode("statType").getValue("rating"), trackerName);
            signs.put(location, leaderboardSign);
        }
    }

    private Optional<Sign> getSign(Location location, boolean showWarning) {
        Optional<BlockEntity> blockEntity = location.getWorld().getBlockEntityAt(location);
        if (!blockEntity.isPresent() || !blockEntity.get().getLocation().getWorld().isType(blockEntity.get(), Sign.class)) {
            if (showWarning)
                Log.warn("Block at location " + location.toString() + " is not a sign!");

            return Optional.empty();
        }

        return blockEntity.map(be -> be.getLocation().getWorld().toType(be, Sign.class));
    }
}
