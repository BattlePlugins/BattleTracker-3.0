package org.battleplugins.tracker.config;

import lombok.AccessLevel;
import lombok.Getter;

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
@Getter
public class ConfigManager {

    @Getter(AccessLevel.NONE)
    private BattleTracker plugin;

    /**
     * The main config.yml for BattleTracker
     *
     * @return the main config.yml
     */
    private Configuration config;

    /**
     * The messages.yml config file for BattleTracker
     *
     * @return the messages.yml config file
     */
    private Configuration messagesConfig;

    /**
     * The signs.yml config file for BattleTracker
     *
     * @return the signs.yml config file
     */
    private Configuration signsConfig;

    /**
     * The pvp.yml config file for BattleTracker
     *
     * @return the pvp.yml config file
     */
    private Configuration pvPConfig;

    /**
     * The pve.yml config file for BattleTracker
     *
     * @return the pve.yml config file
     */
    private Configuration pvEConfig;

    /**
     * The signs.yml save file for BattleTracker
     *
     * @return the signs.yml save file
     */
    private Configuration signSaves;

    public ConfigManager(BattleTracker plugin) {
        this.plugin = plugin;

        loadConfigs();
    }

    /**
     * Loads all the configs necessary for BattleTracker
     */
    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File trackerFolder = new File(plugin.getDataFolder(), "tracking");
        if (!trackerFolder.exists()) {
            trackerFolder.mkdir();
        }

        File savesFolder = new File(plugin.getDataFolder(), "saves");
        if (!savesFolder.exists()) {
            savesFolder.mkdir();
        }

        config = loadConfig(plugin.getDataFolder(), "", "config.yml");
        messagesConfig = loadConfig(plugin.getDataFolder(), "", "messages.yml");
        signsConfig = loadConfig(plugin.getDataFolder(), "", "signs.yml");

        pvPConfig = loadConfig(trackerFolder, "tracking/", "pvp.yml");
        pvEConfig = loadConfig(trackerFolder, "tracking/", "pve.yml");

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
                InputStream stream = plugin.getClass().getResourceAsStream("/" + resourceDir + resource);
                if (stream == null) {
                    configFile.createNewFile();
                    Log.debug("Did not find " + resource + " in the jar, so creating an empty file instead.");
                } else {
                    FileUtil.writeFile(configFile, plugin.getClass().getResourceAsStream("/" + resourceDir + resource));
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
        reloadConfig(pvPConfig);
        reloadConfig(pvEConfig);
        reloadConfig(signSaves);
    }

    /**
     * Savesall the config files for BattleTracker
     */
    public void saveConfigs() {
        config.save();
        messagesConfig.save();
        signsConfig.save();
        pvPConfig.save();
        pvEConfig.save();
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
}
