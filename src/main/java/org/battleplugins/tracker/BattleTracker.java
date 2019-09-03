package org.battleplugins.tracker;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.configuration.ConfigurationSection;
import mc.alk.battlecore.util.FileUtil;
import mc.alk.battlecore.util.Log;
import mc.alk.mc.command.MCCommand;
import mc.alk.mc.plugin.MCPlugin;
import org.battleplugins.tracker.executor.TrackerExecutor;
import org.battleplugins.tracker.impl.Tracker;
import org.battleplugins.tracker.message.DeathMessageManager;
import org.battleplugins.tracker.sql.SQLInstance;
import org.battleplugins.tracker.stat.calculator.EloCalculator;
import org.battleplugins.tracker.stat.calculator.RatingCalculator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Overall main class for the BattleTracker plugin.
 *
 * @author Zach443, Redned
 */
public final class BattleTracker {

    public static String PVP_INTERFACE = "PvP";
    public static String PVE_INTERFACE = "PvE";

    private static BattleTracker instance;

    private MCPlugin platform;
    private TrackerManager trackerManager;

    private Configuration config;
    private Configuration messagesConfig;

    private Configuration pvpConfig;
    private Configuration pveConfig;

    private RatingCalculator defaultCalculator;

    public BattleTracker(MCPlugin platform) {
        this.platform = platform;
        this.trackerManager = new TrackerManager();

        loadConfigs();

        boolean trackPvP = pvpConfig.getBoolean("enabled", true);
        boolean trackPvE = pveConfig.getBoolean("enabled", true);

        trackerManager.setTrackPvP(trackPvP);
        trackerManager.setTrackPvE(trackPvE);

        PVP_INTERFACE = pvpConfig.getString("name");
        PVE_INTERFACE = pveConfig.getString("name");

        ConfigurationSection section = config.getSection("database");

        String type = section.getString("type", "sqlite");
        String prefix = section.getString("prefix", "bt_");
        String url = section.getString("url", "localhost");
        String database = section.getString("db", "tracker");
        String port = section.getString("port", "3306");
        String username = section.getString("username", "root");
        String password = section.getString("password");

        SQLInstance.TYPE = type;
        SQLInstance.TABLE_PREFIX = prefix;
        SQLInstance.DATABASE = database;
        SQLInstance.URL = url;
        SQLInstance.PORT = port;
        SQLInstance.USERNAME = username;
        SQLInstance.PASSWORD = password;

        switch (config.getString("rating.calculator")) {
            case "elo":
                this.defaultCalculator = new EloCalculator(config.getFloat("rating.options.elo.default", 1250), config.getFloat("rating.options.elo.spread", 400));
                break;
            default:
                this.defaultCalculator = new EloCalculator(1250, 400);
        }

        if (trackPvP) {
            Tracker tracker = new Tracker(PVP_INTERFACE, defaultCalculator, new HashMap<>());
            tracker.setMessageManager(new DeathMessageManager(tracker, pvpConfig));
            trackerManager.addInterface(PVP_INTERFACE, tracker);

            MCCommand pvpCommand = new MCCommand(pvpConfig.getString("options.command", "pvp"), "Main " + PVP_INTERFACE + " executor.", "battletracker.pvp", new ArrayList<>());
            platform.registerMCCommand(pvpCommand, new TrackerExecutor(this, PVP_INTERFACE));
        }

        if (trackPvE) {
            Tracker tracker = new Tracker(PVE_INTERFACE, defaultCalculator, new HashMap<>());
            tracker.setMessageManager(new DeathMessageManager(tracker, pveConfig));
            trackerManager.addInterface(PVE_INTERFACE, tracker);

            MCCommand pveCommand = new MCCommand(pveConfig.getString("options.command", "pve"), "Main " + PVE_INTERFACE + " executor.", "battletracker.pve", new ArrayList<>());
            platform.registerMCCommand(pveCommand, new TrackerExecutor(this, PVE_INTERFACE));
        }
    }

    /**
     * Returns the TrackerManager instance
     *
     * @return the TrackerManager instance
     */
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    /**
     * Returns the default rating calculator
     *
     * @return the default rating calculator
     */
    public RatingCalculator getDefaultCalculator() {
        return defaultCalculator;
    }

    /**
     * Returns the current platform 
     *
     * @return the current platform
     */
    public MCPlugin getPlatform() {
        return platform;
    }

    /**
     * Returns the current BattleTracker instance
     *
     * @return the current BattleTracker instance
     */
    public static BattleTracker getInstance() {
        return instance;
    }

    /**
     * Sets the current BattleTracker singleton. Cannot
     * be done if it's already set
     *
     * @param tracker the BattleTracker instance to set
     */
    public static void setInstance(BattleTracker tracker) {
        if (instance != null)
            throw new UnsupportedOperationException("Cannot redefine singleton BattleTracker!");

            instance = tracker;
    }

    /**
     * Loads the config files
     */
    private void loadConfigs() {
        if (!platform.getDataFolder().exists()) {
            platform.getDataFolder().mkdir();
        }

        File configFile = new File(platform.getDataFolder(), "config.yml");
        File messagesFile = new File(platform.getDataFolder(), "messages.yml");

        File trackerFolder = new File(platform.getDataFolder(), "tracking");
        if (!trackerFolder.exists()) {
            trackerFolder.mkdir();
        }

        File pvpFile = new File(trackerFolder, "pvp.yml");
        File pveFile = new File(trackerFolder, "pve.yml");

        if (!configFile.exists()) {
            try {
                FileUtil.writeFile(configFile, getClass().getResourceAsStream("/config.yml"));
            } catch (IOException ex) {
                Log.err("Could not create config.yml config file!");
                ex.printStackTrace();
            }
        }

        if (!messagesFile.exists()) {
            try {
                FileUtil.writeFile(messagesFile, getClass().getResourceAsStream("/messages.yml"));
            } catch (IOException ex) {
                Log.err("Could not create messages.yml config file!");
                ex.printStackTrace();
            }
        }

        if (!pvpFile.exists()) {
            try {
                FileUtil.writeFile(pvpFile, getClass().getResourceAsStream("/tracking/pvp.yml"));
            } catch (IOException ex) {
                Log.err("Could not create pvp.yml config file!");
                ex.printStackTrace();
            }
        }

        if (!pveFile.exists()) {
            try {
                FileUtil.writeFile(pveFile, getClass().getResourceAsStream("/tracking/pve.yml"));
            } catch (IOException ex) {
                Log.err("Could not create pve.yml config file!");
                ex.printStackTrace();
            }
        }

        config = new Configuration(configFile);
        messagesConfig = new Configuration(messagesFile);

        pvpConfig = new Configuration(pvpFile);
        pveConfig = new Configuration(pveFile);
    }

    /**
     * Returns the main config.yml for BattleTracker
     *
     * @return the main config.yml
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Returns the messages.yml config file for BattleTracker
     *
     * @return the messages.yml config file
     */
    public Configuration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Returns the pvp.yml config file for BattleTracker
     *
     * @return the pvp.yml config file
     */
    public Configuration getPvPConfig() {
        return pvpConfig;
    }

    /**
     * Returns the pve.yml config file for BattleTracker
     *
     * @return the pve.yml config file
     */
    public Configuration getPvEConfig() {
        return pveConfig;
    }
}
