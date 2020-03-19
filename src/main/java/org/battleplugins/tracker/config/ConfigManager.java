package org.battleplugins.tracker.config;

import lombok.AccessLevel;
import lombok.Getter;

import mc.alk.battlecore.util.Log;

import org.battleplugins.api.configuration.Configuration;
import org.battleplugins.api.configuration.ConfigurationProvider;
import org.battleplugins.tracker.BattleTracker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        try {
            loadConfigs();
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to load configs!");
            ex.printStackTrace();
        }
    }

    /**
     * Loads all the configs necessary for BattleTracker
     */
    public void loadConfigs() throws IOException {
        if (Files.notExists(plugin.getDataFolder())) {
            Files.createDirectories(plugin.getDataFolder());
        }

        Path trackerFolder = Paths.get(plugin.getDataFolder().toString(), "tracking");
        if (Files.notExists(trackerFolder)) {
            Files.createDirectories(trackerFolder);
        }

        Path savesFolder = Paths.get(plugin.getDataFolder().toString(), "saves");
        if (Files.notExists(savesFolder)) {
            Files.createDirectories(savesFolder);
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
    public Configuration loadConfig(Path directory, String resourceDir, String resource) {
        Path configPath = Paths.get(directory.toString(), resource);
        if (Files.notExists(configPath)) {
            try {
                URI uri = this.getClass().getResource(resourceDir + resource).toURI();
                Path path = Paths.get(uri);
                if (Files.notExists(path)) {
                    Files.copy(path, configPath);
                    Log.debug("Did not find " + resource + " in the jar, so creating an empty file instead.");
                }
            } catch (IOException | URISyntaxException ex) {
                Log.err("Could not create " + resource + " config file!");
                ex.printStackTrace();
            }
        }

        return Configuration.builder()
                .path(configPath)
                .provider(ConfigurationProvider.class)
                .build();
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
        try {
            config.save();
            messagesConfig.save();
            signsConfig.save();
            pvPConfig.save();
            pvEConfig.save();
            signSaves.save();
        } catch (IOException ex) {
            Log.warn("Failed to save configs!");
            ex.printStackTrace();
        }
    }

    /**
     * Reloads a config file
     *
     * @param config the config to reload
     */
    public void reloadConfig(Configuration config) {
        try {
        config.save();
        config.reload();
        } catch (IOException ex) {
            Log.warn("Failed to config file!");
            ex.printStackTrace();
        }
    }
}
