package org.battleplugins.tracker;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.configuration.ConfigurationSection;
import mc.alk.battlecore.util.Log;
import mc.alk.mc.APIType;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.command.MCCommand;
import mc.alk.mc.plugin.MCPlugin;
import mc.alk.mc.plugin.MCServicePriority;
import mc.alk.mc.plugin.PluginProperties;
import org.battleplugins.tracker.bukkit.BukkitCodeHandler;
import org.battleplugins.tracker.config.ConfigManager;
import org.battleplugins.tracker.executor.TrackerExecutor;
import org.battleplugins.tracker.impl.Tracker;
import org.battleplugins.tracker.message.DeathMessageManager;
import org.battleplugins.tracker.message.MessageManager;
import org.battleplugins.tracker.nukkit.NukkitCodeHandler;
import org.battleplugins.tracker.sponge.SpongeCodeHandler;
import org.battleplugins.tracker.sql.SQLInstance;
import org.battleplugins.tracker.stat.calculator.EloCalculator;
import org.battleplugins.tracker.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.util.DependencyUtil;
import org.battleplugins.tracker.util.DependencyUtil.DownloadResult;
import org.battleplugins.tracker.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Overall main class for the BattleTracker plugin.
 *
 * @author Zach443, Redned
 */
@PluginProperties(id = "bt", authors = "BattlePlugins", name = TrackerInfo.NAME, version = TrackerInfo.VERSION, description = TrackerInfo.DESCRIPTION, url = TrackerInfo.URL)
public final class BattleTracker extends MCPlugin {

    public static String PVP_INTERFACE = "PvP";
    public static String PVE_INTERFACE = "PvE";

    private static BattleTracker instance;

    private ConfigManager configManager;
    private TrackerManager trackerManager;
    private MessageManager messageManager;

    private RatingCalculator defaultCalculator;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("You are running " + TrackerInfo.NAME + " on " + Util.capitalizeFirst(MCPlatform.getType().name()) + "!");
        DependencyUtil.setLibFolder(new File(getDataFolder(), "libraries"));
        DependencyUtil.downloadDepedencies().whenComplete((result, action) -> {
            if (result != DownloadResult.SUCCESS) {
                getLogger().severe("Unable to download SQL libraries for BattleTracker!");
                getLogger().severe("If this error persists, there may be a restraint on your host or server provider.");
                getLogger().severe("Please view the tutorial on how to download the libraries manually on the BattlePlugins documentation.");
                MCPlatform.getPluginManager().disablePlugin();
                return;
            }

            this.configManager = new ConfigManager(this);
            this.trackerManager = new TrackerManager();

            // Register the tracker manager into the service provider API
            MCPlatform.registerService(TrackerManager.class, trackerManager, this, MCServicePriority.NORMAL);

            this.messageManager = new MessageManager("messages", "special", configManager.getMessagesConfig());

            boolean trackPvP = configManager.getPvPConfig().getBoolean("enabled", true);
            boolean trackPvE = configManager.getPvEConfig().getBoolean("enabled", true);

            trackerManager.setTrackPvP(trackPvP);
            trackerManager.setTrackPvE(trackPvE);

            PVP_INTERFACE = configManager.getPvPConfig().getString("name");
            PVE_INTERFACE = configManager.getPvEConfig().getString("name");

            ConfigurationSection section = configManager.getConfig().getSection("database");

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
            SQLInstance.PORT = port;
            SQLInstance.USERNAME = username;
            SQLInstance.PASSWORD = password;

            if (type.equalsIgnoreCase("sqlite"))
                SQLInstance.URL = getDataFolder().toString();
            else
                SQLInstance.URL = url;

            switch (configManager.getConfig().getString("rating.calculator")) {
                case "elo":
                    this.defaultCalculator = new EloCalculator(configManager.getConfig().getFloat("rating.options.elo.default", 1250), configManager.getConfig().getFloat("rating.options.elo.spread", 400));
                    break;
                default:
                    this.defaultCalculator = new EloCalculator(1250, 400);
            }

            if (trackPvP) {
                Tracker tracker = new Tracker(PVP_INTERFACE, new DeathMessageManager(configManager.getPvPConfig()), defaultCalculator, new HashMap<>());
                trackerManager.addInterface(PVP_INTERFACE, tracker);

                MCCommand pvpCommand = new MCCommand(configManager.getPvPConfig().getString("options.command", "pvp"), "Main " + PVP_INTERFACE + " executor.", "battletracker.pvp", new ArrayList<>());
                registerCommand(pvpCommand, new TrackerExecutor(this, PVP_INTERFACE));
            }

            if (trackPvE) {
                Tracker tracker = new Tracker(PVE_INTERFACE, new DeathMessageManager(configManager.getPvEConfig()), defaultCalculator, new HashMap<>());
                trackerManager.addInterface(PVE_INTERFACE, tracker);

                MCCommand pveCommand = new MCCommand(configManager.getPvEConfig().getString("options.command", "pve"), "Main " + PVE_INTERFACE + " executor.", "battletracker.pve", new ArrayList<>());
                registerCommand(pveCommand, new TrackerExecutor(this, PVE_INTERFACE));
            }

            APIType api = MCPlatform.getType();
            if (api == APIType.BUKKIT)
                platformCode.put(APIType.BUKKIT, new BukkitCodeHandler(this));

            if (api == APIType.NUKKIT)
                platformCode.put(APIType.NUKKIT, new NukkitCodeHandler(this));

            if (api == APIType.SPONGE)
                platformCode.put(APIType.SPONGE, new SpongeCodeHandler(this));

            Log.setPlugin(this);
            Log.setDebug(configManager.getConfig().getBoolean("debugMode", false));
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving all records...");
        try {
            for (TrackerInterface trackerInterface : trackerManager.getInterfaces().values()) {
                trackerInterface.saveAll();
            }
            getLogger().info("Saved all records successfully!");
        } catch (Exception ex) {
            getLogger().error("Could not save all records! Please make sure everything is configured correctly!");
            getLogger().error("If this error persists, please open a report on GitHub!");
            ex.printStackTrace();
        }
    }

    /**
     * Returns the configuration manager
     *
     * @return the configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Returns the config for BattleTracker
     *
     * @return the config for BattleTracker
     */
    public Configuration getConfig() {
        return configManager.getConfig();
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
     * Returns the MessageManager instance
     *
     * @return the MessageManager instance
     */
    public MessageManager getMessageManager() {
        return messageManager;
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
     * Returns the BattleTracker instance
     *
     * @return the BattleTracker instance
     */
    public static BattleTracker getInstance() {
        return instance;
    }
}
