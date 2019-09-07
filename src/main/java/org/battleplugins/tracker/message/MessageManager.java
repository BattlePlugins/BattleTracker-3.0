package org.battleplugins.tracker.message;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.controllers.MessageController;
import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Main message manager for BattleTracker.
 *
 * @author Redned
 */
public class MessageManager {

    private Map<String, String> messages;

    public MessageManager() {
        this.messages = new HashMap<>();
    }

    public MessageManager(String path, Configuration config) {
        this();

        for (String str : config.getSection(path).getKeys(false)) {
            messages.put(str, config.getString(path + "." + str));
        }
    }

    /**
     * Returns a message with the given key/path
     *
     * @param key the name (key) of the message in the config
     * @return a message with the given key/path
     */
    public String getMessage(String key) {
        return messages.get(key);
    }

    /**
     * Returns a message with the given key/path which
     * is formatted with color replacements and the
     * prefix
     *
     * @param key the name (key) of the message in the config
     * @return a message with the given key/path
     */
    public String getFormattedMessage(String key) {
        return MessageController.colorChat(messages.get("prefix") + MessageController.colorChat(messages.get(key)));
    }

    /**
     * Returns a message with the given key/path and
     * variable replacement for the specified player
     *
     * @param player the player to perform the variable replacement on
     * @param key the name (key) of the message in the config
     * @return a message with the given key/path
     */
    public String getMessage(MCOfflinePlayer player, String key) {
        return getPlaceholderMessage(player, messages.get(key));
    }

    /**
     * Returns a message with the given key/path and
     * variable replacement for the specified player,
     * which is also formatted with color replacements
     * and prefix
     *
     * @param player the player to perform the variable replacement on
     * @param key the name (key) of the message in the config
     * @return a message with the given key/path
     */
    public String getFormattedMessage(MCOfflinePlayer player, String key) {
        return MessageController.colorChat(messages.get("prefix") + getPlaceholderMessage(player, messages.get(key)));
    }

    /**
     * Returns the message with variables replaced
     *
     * @param player the player to base variable replacement from
     * @param message the message to send
     * @return the message with variables replaced
     */
    public String getPlaceholderMessage(MCOfflinePlayer player, String message) {
        message = message.replace("%player_name%", player.getName());
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            message = message.replace("%" + entry + "%", entry.getValue());
        }
        return MessageController.colorChat(message);
    }

    /**
     * Sends a message to the specified player with
     * the variables replaced
     *
     * @param player the player to send the message to
     * @param message the message to send
     */
    public void sendMessage(MCPlayer player, String message) {
        player.sendMessage(getPlaceholderMessage(player, message));
    }
}
