package org.battleplugins.tracker.message;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.controllers.MessageController;
import mc.alk.mc.ChatColor;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.chat.HoverAction;
import mc.alk.mc.chat.Message;
import mc.alk.mc.chat.MessageBuilder;
import mc.alk.mc.entity.MCEntity;
import org.battleplugins.tracker.TrackerInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main death message manager for BattleTracker.
 *
 * @author Redned
 */
public class DeathMessageManager {

    private TrackerInterface tracker;

    private boolean enabled;
    private String prefix;

    // Options
    private boolean overrideBukkitMessages;
    private boolean useHoverMessages;
    private boolean useClickMessages;

    private String hoverContent;
    private String clickContent;

    private int msgRadius;

    private Map<String, List<String>> itemMessages;
    private Map<String, List<String>> entityMessages;
    private Map<String, List<String>> causeMessages;

    public DeathMessageManager(TrackerInterface tracker) {
        this.tracker = tracker;

        this.enabled = true;
        this.prefix = "[Tracker]";

        this.overrideBukkitMessages = true;
        this.useHoverMessages = true;
        this.useClickMessages = true;

        this.hoverContent = "all";
        this.clickContent = "all";

        this.msgRadius = 0;

        this.itemMessages = new HashMap<>();
        this.entityMessages = new HashMap<>();
        this.causeMessages = new HashMap<>();
    }

    public DeathMessageManager(TrackerInterface tracker, Configuration config) {
        this.tracker = tracker;

        loadDataFromConfig(config);
    }

    /**
     * Loads message data from the specified config file
     *
     * @param config the config file
     */
    public void loadDataFromConfig(Configuration config) {
        this.enabled = config.getBoolean("messages.enabled");
        this.prefix = MessageController.colorChat(config.getString("prefix"));
        this.overrideBukkitMessages = config.getBoolean("options.overrideBukkitMessages");
        this.useHoverMessages = config.getBoolean("options.useHoverMessages");
        this.useClickMessages = config.getBoolean("options.useClickMessages");

        this.hoverContent = config.getString("options.hoverContent");
        this.clickContent = config.getString("options.clickContent");

        this.msgRadius = config.getInt("options.msgRadius");

        entityMessages = new HashMap<>();
        for (String str : config.getSection("messages.entityDeaths").getKeys(false)) {
            entityMessages.put(str, config.getStringList("messages.entityDeaths." + str));
        }

        causeMessages = new HashMap<>();
        for (String str : config.getSection("messages.causeDeaths").getKeys(false)) {
            causeMessages.put(str, config.getStringList("messages.causeDeaths." + str));
        }

        itemMessages = new HashMap<>();
        for (String str : config.getSection("messages").getKeys(false)) {
            // Item messages are just defined right in the messages section, so check for other sections first
            if (str.equalsIgnoreCase("entityDeaths"))
                continue;

            if (str.equalsIgnoreCase("causeDeaths"))
                continue;

            if (str.equalsIgnoreCase("enabled"))
                continue;

            itemMessages.put(str, config.getStringList("messages." + str));
        }
    }

    /**
     * Returns if the message manager is enabled
     *
     * @return if the message manager is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets if the message manager is enabled
     *
     * @param enabled if the message manager is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns if Bukkit death messages should be overridden
     *
     * @return if Bukkit death messages should be overridden
     */
    public boolean shouldOverrideBukkitMessages() {
        return overrideBukkitMessages;
    }

    /**
     * Sets if Bukkit death messages should be overridden
     *
     * @param overrideBukkitMessages if Bukkit death messages should be overridden
     */
    public void setOverrideBukkitMessages(boolean overrideBukkitMessages) {
        this.overrideBukkitMessages = overrideBukkitMessages;
    }

    /**
     * Sends a death message relating to the item used to kill
     * an entity or another player
     *
     * @param killerName the name of the killer (can be an entity or cause)
     * @param killedName the name of the killed player
     * @param itemName the name of the item used to kill the target
     */
    public void sendItemMessage(String killerName, String killedName, String itemName) {
        if (!enabled)
            return;

        List<String> items = itemMessages.get(itemName);
        if (items == null || items.isEmpty())
            items = itemMessages.get("unknown"); // item isn't in list

        // At this point they want these specific messages disabled
        if (items == null)
            return;

        MCPlayer killed = MCPlatform.getPlayer(killerName);
        if (killed == null)
            return;

        Random random = new Random();
        String message = items.get(random.nextInt(items.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        MessageBuilder messageBuilder = new MessageBuilder(prefix + message);
        attachHoverEvent(messageBuilder, killed);
        attachClickEvent(messageBuilder, killed);

        sendDeathMessage(killed, messageBuilder.build());
    }

    /**
     * Sends a death message relating to the entity a player
     * was killed by
     *
     * @param killerName the name of the killer (name of entity)
     * @param killedName the name of the killed player
     * @param itemName the item the mob had in its hand at the time of the player's death
     */
    public void sendEntityMessage(String killerName, String killedName, String itemName) {
        if (!enabled)
            return;

        List<String> entities = entityMessages.get(killerName);
        if (entities == null || entities.isEmpty())
            entities = entityMessages.get("unknown"); // entity isn't in list

        // At this point they want these specific messages disabled
        if (entities == null)
            return;

        MCPlayer killed = MCPlatform.getPlayer(killedName);
        if (killed == null)
            return;

        Random random = new Random();
        String message = entities.get(random.nextInt(entities.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        MessageBuilder messageBuilder = new MessageBuilder(prefix + message);
        attachHoverEvent(messageBuilder, killed);
        attachClickEvent(messageBuilder, killed);

        sendDeathMessage(killed, messageBuilder.build());
    }

    /**
     * Sends a death message relating to why the player
     * died (if not an entity or with an item)
     *
     * @param killerName the name of the killer (name of cause)
     * @param killedName the name of the killed player
     * @param itemName the item the player currently has in their hand
     */
    public void sendCauseMessage(String killerName, String killedName, String itemName) {
        if (!enabled)
            return;

        List<String> causes = causeMessages.get(killerName);
        if (causes == null || causes.isEmpty())
            causes = causeMessages.get("unknown"); // cause isn't in list

        // At this point they want these specific messages disabled
        if (causes == null)
            return;

        MCPlayer killed = MCPlatform.getPlayer(killedName);
        if (killed == null)
            return;

        Random random = new Random();
        String message = causes.get(random.nextInt(causes.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        MessageBuilder messageBuilder = new MessageBuilder(prefix + message);
        attachHoverEvent(messageBuilder, killed);
        attachClickEvent(messageBuilder, killed);

        sendDeathMessage(killed, messageBuilder.build());
    }

    /**
     * Does a variable replacement of the death message
     * with the specified parameters.
     *
     * %k : the killer (can be a player, mob, or environment)
     * %d : the dead player
     * %i : item used to kill the player (if one exists)
     * %n : number (used for streaks, rampages)
     *
     * @param message the message to replace variables in
     * @param killer the name of the killer
     * @param killed the name of the player killed
     * @param itemName the item in the killer or killed player's hand (varies from usage)
     * @param streak the streak of the killer/killed player (varies from usage)
     * @return the replaced message
     */
    public String replacePlaceholders(String message, String killer, String killed, String itemName, int streak) {
        message = message.replace("%k", killer);
        message = message.replace("%d", killed);
        message = message.replace("%i", itemName);
        message = message.replace("%n", String.valueOf(streak));
        return message;
    }

    // TODO: Implement recap option
    private void attachHoverEvent(MessageBuilder messageBuilder, MCPlayer player) {
        if (!useHoverMessages)
            return;

        String hoverMessage = "";
        switch (hoverContent) {
            case "all":
                hoverMessage += ChatColor.GOLD + "Armor:"
                        + "\n" + ChatColor.GRAY + "Helmet: " + ChatColor.YELLOW + player.getInventory().getHelmet().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Chestplate: " + ChatColor.YELLOW + player.getInventory().getChestplate().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Leggings: " + ChatColor.YELLOW + player.getInventory().getLeggings().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Boots: " + ChatColor.YELLOW + player.getInventory().getBoots().getFormattedCommonName();

                hoverMessage += "\n \n" + ChatColor.GOLD + "Recap: ";
                break;
            case "armor":
                hoverMessage += ChatColor.GOLD + "Armor:"
                        + "\n" + ChatColor.GRAY + "Helmet: " + ChatColor.YELLOW + player.getInventory().getHelmet().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Chestplate: " + ChatColor.YELLOW + player.getInventory().getChestplate().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Leggings: " + ChatColor.YELLOW + player.getInventory().getLeggings().getFormattedCommonName()
                        + "\n" + ChatColor.GRAY + "Boots: " + ChatColor.YELLOW + player.getInventory().getBoots().getFormattedCommonName();

                break;
            case "recap":
                break;
            case "none":
                break;
        }

        if (hoverMessage.isEmpty())
            return;

        messageBuilder.setHoverAction(HoverAction.SHOW_TEXT);
        messageBuilder.setHoverMessage(hoverMessage);
    }

    private void attachClickEvent(MessageBuilder messageBuilder, MCPlayer player) {
        if (!useClickMessages)
            return;

        // TODO: Add click event support (requires inventory/modal support for McAPI)
    }

    private void sendDeathMessage(MCPlayer source, Message message) {
        if (msgRadius == 0) {
            MCPlatform.broadcastMessage(message);
        } else {
            for (MCEntity entity : source.getNearbyEntities(msgRadius, msgRadius, msgRadius)) {
                if (!(entity instanceof MCPlayer))
                    continue;

                MCPlayer radiusPlayer = (MCPlayer) entity;
                radiusPlayer.sendMessage(message);
            }
        }
    }
}
