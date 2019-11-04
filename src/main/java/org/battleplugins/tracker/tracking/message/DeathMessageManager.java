package org.battleplugins.tracker.tracking.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import mc.alk.battlecore.configuration.Configuration;
import mc.alk.battlecore.controllers.MessageController;
import mc.alk.mc.ChatColor;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.chat.ClickAction;
import mc.alk.mc.chat.HoverAction;
import mc.alk.mc.chat.Message;
import mc.alk.mc.chat.MessageBuilder;
import mc.alk.mc.entity.MCEntity;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.recap.DamageInfo;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.util.TrackerUtil;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Main death message manager for BattleTracker, mainly
 * used in individual trackers.
 *
 * @author Redned
 */
@Getter
public class DeathMessageManager {

    @Getter(AccessLevel.NONE)
    private BattleTracker plugin;

    @Getter(AccessLevel.NONE)
    private TrackerInterface tracker;

    /**
     * If the message manager is enabled
     *
     * @param enabled if the message manager is enabled
     * @return if the message manager is enabled
     */
    @Setter
    private boolean enabled;

    /**
     * The prefix to use in death messages
     *
     * @return the prefix to use in death messages
     */
    private String prefix;

    /**
     * If default death messages should be overridden
     *
     * @return if default death messages should be overridden
     */
    private boolean defaultMessagesOverriden;

    /**
     * If hover messages are enabled
     *
     * @return if hover messages are enabled
     */
    private boolean hoverMessagesEnabled;

    /**
     * If click messages are enabled
     *
     * @return if click messages are enabled
     */
    private boolean clickMessagesEnabled;

    /**
     * The hover content option
     *
     * @return the hover content option
     */
    private String hoverContent;

    /**
     * Returns the click content option
     *
     * @return the click content option
     */
    private String clickContent;

    /**
     * The radius in which to send messages
     *
     * @return the radius in which to send messages
     */
    private int msgRadius;

    private Map<String, List<String>> itemMessages;
    private Map<String, List<String>> entityMessages;
    private Map<String, List<String>> causeMessages;

    public DeathMessageManager(BattleTracker plugin, TrackerInterface tracker, Configuration config) {
        this.plugin = plugin;
        this.tracker = tracker;

        loadDataFromConfig(config);
    }

    /**
     * Loads message data from the specified config file
     *
     * @param config the config file
     */
    public void loadDataFromConfig(Configuration config) {
        this.enabled = config.getBoolean("messages.enabled", true);
        this.prefix = MessageController.colorChat(config.getString("prefix", "[Tracker]"));
        this.defaultMessagesOverriden = config.getBoolean("options.overrideDefaultMessages", true);
        this.hoverMessagesEnabled = config.getBoolean("options.useHoverMessages", true);
        this.clickMessagesEnabled = config.getBoolean("options.useClickMessages", true);

        this.hoverContent = config.getString("options.hoverContent", "all");
        this.clickContent = config.getString("options.clickContent", "all");

        this.msgRadius = config.getInt("options.msgRadius", 0);

        this.entityMessages = new HashMap<>();
        for (String str : config.getSection("messages.entityDeaths").getKeys(false)) {
            entityMessages.put(str, config.getStringList("messages.entityDeaths." + str));
        }

        this.causeMessages = new HashMap<>();
        for (String str : config.getSection("messages.causeDeaths").getKeys(false)) {
            causeMessages.put(str, config.getStringList("messages.causeDeaths." + str));
        }

        this.itemMessages = new HashMap<>();
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

        MCPlayer killed = plugin.getPlatform().getPlayer(killedName);
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

        MCPlayer killed = plugin.getPlatform().getPlayer(killedName);
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

        List<String> causes = causeMessages.get(killedName);
        if (causes == null || causes.isEmpty())
            causes = causeMessages.get("unknown"); // cause isn't in list

        // At this point they want these specific messages disabled
        if (causes == null)
            return;

        MCPlayer killed = plugin.getPlatform().getPlayer(killedName);
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

    private void attachHoverEvent(MessageBuilder messageBuilder, MCPlayer player) {
        if (!hoverMessagesEnabled)
            return;

        Recap recap = tracker.getRecapManager().getDeathRecaps().get(player.getName());
        String hoverMessage = "";
        switch (hoverContent) {
            case "all":
                hoverMessage += getArmorHoverMessage(player);
                hoverMessage += "\n" + getRecapHoverMessage(recap);
                break;
            case "armor":
                hoverMessage += getArmorHoverMessage(player);
                break;
            case "recap":
                hoverMessage += getRecapHoverMessage(recap);
                break;
            case "none":
                return;
        }

        if (hoverMessage.isEmpty())
            return;

        messageBuilder.setHoverAction(HoverAction.SHOW_TEXT);
        messageBuilder.setHoverMessage(hoverMessage);
    }

    private void attachClickEvent(MessageBuilder messageBuilder, MCPlayer player) {
        if (!clickMessagesEnabled || clickContent.equalsIgnoreCase("none"))
            return;

        messageBuilder.setClickAction(ClickAction.RUN_COMMAND);
        messageBuilder.setClickValue("/" + tracker.getName() + " recap " + player.getName());
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

    private String getRecapHoverMessage(Recap recap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String hoverMessage = ChatColor.GOLD + "Damage Recap:";

        // Obtains last 10 damages dealt
        List<DamageInfo> damageInfos = recap.getLastDamages().stream().sorted((recap1, recap2) -> (int) (recap2.getLogTime() - recap1.getLogTime())).collect(Collectors.toList());
        int max = damageInfos.size();
        if (max > 10)
            max = 10;

        for (int i = damageInfos.size() - max; i < damageInfos.size(); i++) {
            DamageInfo damageInfo = damageInfos.get(i);
            hoverMessage += "\n" + ChatColor.RED + " ♥ -" + decimalFormat.format(damageInfo.getDamage() / 2) + " " + ChatColor.YELLOW + ChatColor.AQUA + TrackerUtil.capitalizeFirst(damageInfo.getCause().replace("_", " "));
        }

        return hoverMessage;
    }

    private String getArmorHoverMessage(MCPlayer player) {
        return ChatColor.GOLD + "Armor:"
                + "\n" + ChatColor.GRAY + " Helmet: " + ChatColor.YELLOW + player.getInventory().getHelmet().getFormattedCommonName()
                + "\n" + ChatColor.GRAY + " Chestplate: " + ChatColor.YELLOW + player.getInventory().getChestplate().getFormattedCommonName()
                + "\n" + ChatColor.GRAY + " Leggings: " + ChatColor.YELLOW + player.getInventory().getLeggings().getFormattedCommonName()
                + "\n" + ChatColor.GRAY + " Boots: " + ChatColor.YELLOW + player.getInventory().getBoots().getFormattedCommonName();
    }
}
