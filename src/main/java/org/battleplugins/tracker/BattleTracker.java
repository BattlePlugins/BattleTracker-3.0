package org.battleplugins.tracker;

import lombok.Getter;

import mc.alk.battlecore.BattlePlugin;
import mc.alk.battlecore.util.Log;

import org.battleplugins.api.Platform;
import org.battleplugins.api.PlatformType;
import org.battleplugins.api.PlatformTypes;
import org.battleplugins.api.command.Command;
import org.battleplugins.api.configuration.Configuration;
import org.battleplugins.api.configuration.ConfigurationNode;
import org.battleplugins.api.plugin.PluginProperties;
import org.battleplugins.api.plugin.service.ServicePriority;
import org.battleplugins.tracker.bukkit.BukkitCodeHandler;
import org.battleplugins.tracker.config.ConfigManager;
import org.battleplugins.tracker.executor.TrackerExecutor;
import org.battleplugins.tracker.tracking.Tracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.TrackerListener;
import org.battleplugins.tracker.tracking.TrackerManager;
import org.battleplugins.tracker.tracking.stat.calculator.EloCalculator;
import org.battleplugins.tracker.tracking.stat.calculator.RatingCalculator;
import org.battleplugins.tracker.message.MessageManager;
import org.battleplugins.tracker.nukkit.NukkitCodeHandler;
import org.battleplugins.tracker.sign.SignManager;
import org.battleplugins.tracker.sign.SignUpdateTask;
import org.battleplugins.tracker.sponge.SpongeCodeHandler;
import org.battleplugins.tracker.sql.SQLInstance;
import org.battleplugins.tracker.util.DependencyUtil;
import org.battleplugins.tracker.util.DependencyUtil.DownloadResult;
import org.battleplugins.tracker.util.TrackerUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Overall main class for the BattleTracker plugin.
 *
 * @author Zach443, Redned
 */
@Getter
@PluginProperties(id = "battletracker", authors = "BattlePlugins", name = TrackerInfo.NAME, version = TrackerInfo.VERSION, description = TrackerInfo.DESCRIPTION, url = TrackerInfo.URL)
public final class BattleTracker extends BattlePlugin {

    public static String PVP_INTERFACE = "PvP";
    public static String PVE_INTERFACE = "PvE";

    private static BattleTracker instance;

    /**
     * The ConfigManager for BattleTracker
     *
     * @return the ConfigManager for BattleTracker
     */
    private ConfigManager configManager;

    /**
     * The TrackerManager instance
     *
     * @return the TrackerManager instance
     */
    private TrackerManager trackerManager;

    /**
     * The MessageManager for BattleTracker
     *
     * @return the MessageManager for BattleTracker
     */
    private MessageManager messageManager;

    /**
     * The SignManager instance
     *
     * @return the SignManager instance
     */
    private SignManager signManager;

    /**
     * The default rating calculator
     *
     * @return the default rating calculator
     */
    private RatingCalculator defaultCalculator;

    @Override
    public void onEnable() {
        instance = this;

        super.onEnable();

        getLogger().info("You are running " + TrackerInfo.NAME + " on " + TrackerUtil.capitalizeFirst(this.getPlatform().getType().getName()) + "!");
        DependencyUtil.setLibFolder(new File(getDataFolder(), "libraries"));
        DependencyUtil.downloadDepedencies().whenComplete((result, action) -> {
            if (result != DownloadResult.SUCCESS) {
                getLogger().severe("Unable to download SQL libraries for BattleTracker!");
                getLogger().severe("If this error persists, there may be a restraint on your host or server provider.");
                getLogger().severe("Please view the tutorial on how to download the libraries manually on the BattlePlugins documentation.");
                Platform.getPluginManager().disablePlugin(this);
                return;
            }

            this.configManager = new ConfigManager(this);
            this.trackerManager = new TrackerManager();
            this.signManager = new SignManager(this);
            this.messageManager = new MessageManager("messages", "special", configManager.getMessagesConfig());

            // Register the tracker manager into the service provider API
            getServer().registerService(TrackerManager.class, trackerManager, this, ServicePriority.NORMAL);

            boolean trackPvP = configManager.getPvPConfig().getNode("enabled").getValue(true);
            boolean trackPvE = configManager.getPvEConfig().getNode("enabled").getValue(true);

            trackerManager.setTrackingPvP(trackPvP);
            trackerManager.setTrackingPvE(trackPvE);

            PVP_INTERFACE = configManager.getPvPConfig().getNode("name").getValue(String.class);
            PVE_INTERFACE = configManager.getPvEConfig().getNode("name").getValue(String.class);

            ConfigurationNode databaseNode = getConfig().getNode("database");
            String type = databaseNode.getNode("type").getValue("sqlite");
            String prefix = databaseNode.getNode("prefix").getValue("bt_");
            String url = databaseNode.getNode("url").getValue("localhost");
            String database = databaseNode.getNode("db").getValue("tracker");
            String port = databaseNode.getNode("port").getValue("3306");
            String username = databaseNode.getNode("username").getValue("root");
            String password = databaseNode.getNode("password").getValue(String.class);

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

            switch (getConfig().getNode("rating.calculator").getValue(String.class)) {
                case "elo":
                    this.defaultCalculator = new EloCalculator(getConfig().getNode("rating.options.elo.default").getValue(1250),
                            getConfig().getNode("rating.options.elo.spread").getValue(400));
                    break;
                default:
                    this.defaultCalculator = new EloCalculator(1250, 400);
            }

            if (trackPvP) {
                Tracker tracker = new Tracker(this, PVP_INTERFACE, configManager.getPvPConfig(), defaultCalculator);
                trackerManager.addInterface(PVP_INTERFACE, tracker);

                Command pvpCommand = new Command(configManager.getPvPConfig().getNode("options.command").getValue("pvp"), "Main " + PVP_INTERFACE + " executor.", "battletracker.pvp", new ArrayList<>());
                registerCommand(pvpCommand, new TrackerExecutor(this, tracker));
            }

            if (trackPvE) {
                Tracker tracker = new Tracker(this, PVE_INTERFACE, configManager.getPvEConfig(), defaultCalculator);
                trackerManager.addInterface(PVE_INTERFACE, tracker);

                Command pveCommand = new Command(configManager.getPvEConfig().getNode("options.command").getValue("pve"), "Main " + PVE_INTERFACE + " executor.", "battletracker.pve", new ArrayList<>());
                registerCommand(pveCommand, new TrackerExecutor(this, tracker));
            }

            this.getEventBus().provideInstance(TrackerListener.class, new TrackerListener(this));

            PlatformType platformType = getPlatform().getType();
            if (platformType == PlatformTypes.BUKKIT)
                platformCode.put(PlatformTypes.BUKKIT, new BukkitCodeHandler(this));

            if (platformType == PlatformTypes.NUKKIT)
                platformCode.put(PlatformTypes.NUKKIT, new NukkitCodeHandler(this));

            if (platformType == PlatformTypes.SPONGE)
                platformCode.put(PlatformTypes.SPONGE, new SpongeCodeHandler(this));

            Log.setDebug(getConfig().getNode("debugMode").getValue(false));

            getPlatform().scheduleRepeatingTask(this, new SignUpdateTask(signManager), 60000); // 1 minute
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            signManager.saveSigns("signs", configManager.getSignSaves());
        } catch (Exception ex) {
            getLogger().warning("Could not save signs!");
            ex.printStackTrace();
        }

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
     * Returns the config for BattleTracker
     *
     * @return the config for BattleTracker
     */
    public Configuration getConfig() {
        return configManager.getConfig();
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
