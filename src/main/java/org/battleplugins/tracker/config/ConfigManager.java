package org.battleplugins.tracker.config;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.util.FileUtil;
import mc.alk.battlecore.util.Log;
import org.battleplugins.tracker.BattleTracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Manages configuration files used across BattleTracker.
 *
 * @author Redned
 */
public class ConfigManager {

    private BattleTracker tracker;

    private Configuration config;
    private Configuration messagesConfig;
    private Configuration signsConfig;

    private Configuration pvpConfig;
    private Configuration pveConfig;

    private Configuration signSaves;

    public ConfigManager(BattleTracker tracker) {
        this.tracker = tracker;

        loadConfigs();
    }

    /**
     * Loads all the configs necessary for BattleTracker
     */
    public void loadConfigs() {
        if (!tracker.getDataFolder().exists()) {
            tracker.getDataFolder().mkdir();
        }

        File trackerFolder = new File(tracker.getDataFolder(), "tracking");
        if (!trackerFolder.exists()) {
            trackerFolder.mkdir();
        }

        File savesFolder = new File(tracker.getDataFolder(), "saves");
        if (!savesFolder.exists()) {
            savesFolder.mkdir();
        }

        config = loadConfig(tracker.getDataFolder(), "", "config.yml");
        messagesConfig = loadConfig(tracker.getDataFolder(), "", "messages.yml");
        signsConfig = loadConfig(tracker.getDataFolder(), "", "signs.yml");

        pvpConfig = loadConfig(trackerFolder, "tracking/", "pvp.yml");
        pveConfig = loadConfig(trackerFolder, "tracking/", "pve.yml");

        signSaves = loadConfig(savesFolder, "saves/", "signs.yml");
    }

    /**
     * Loads a config file, attempts to load from
     * jar if not found
     *
     * @param directory the file of the config
     * @param resourceDir the directory of the resource
     * @param resource the resource to load from
     * @return the configuration object of the file
     */
    public Configuration loadConfig(File directory, String resourceDir, String resource) {
        File configFile = new File(directory, resource);
        if (!configFile.exists()) {
            try {
                InputStream stream = tracker.getClass().getResourceAsStream("/" + resourceDir + resource);
                if (stream == null) {
                    configFile.createNewFile();
                    Log.debug("Did not find " + resource + " in the jar, so creating an empty file instead.");
                } else {
                    FileUtil.writeFile(configFile, tracker.getClass().getResourceAsStream("/" + resourceDir + resource));
                }
            } catch (IOException ex) {
                Log.err("Could not create " + resource + " config file!");
                ex.printStackTrace();
            }
        }

        return new Configuration(configFile);
    }

    /**
     * Reloads all the config files for BattleTracker
     */
    public void reloadConfigs() {
        reloadConfig(config);
        reloadConfig(messagesConfig);
        reloadConfig(signsConfig);
        reloadConfig(pvpConfig);
        reloadConfig(pveConfig);
        reloadConfig(signSaves);
    }

    /**
     * Savesall the config files for BattleTracker
     */
    public void saveConfigs() {
        config.save();
        messagesConfig.save();
        signsConfig.save();
        pvpConfig.save();
        pveConfig.save();
        signSaves.save();
    }

    /**
     * Reloads a config file
     *
     * @param config the config to reload
     */
    public void reloadConfig(Configuration config) {
        config.save();
        config.reload();
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
     * Returns the signs.yml config file for BattleTracker
     *
     * @return the signs.yml config file
     */
    public Configuration getSignsConfig() {
        return signsConfig;
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

    /**
     * Returns the signs.yml save file for BattleTracker
     *
     * @return the signs.yml save file
     */
    public Configuration getSignSaves() {
        return signSaves;
    }
}
