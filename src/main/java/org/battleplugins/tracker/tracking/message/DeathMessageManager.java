package org.battleplugins.tracker.tracking.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import mc.alk.battlecore.message.MessageController;

import org.apache.commons.lang.WordUtils;
import org.battleplugins.api.configuration.Configuration;
import org.battleplugins.api.entity.Entity;
import org.battleplugins.api.entity.living.player.Player;
import org.battleplugins.api.inventory.item.ItemStack;
import org.battleplugins.api.inventory.item.component.DisplayNameComponent;
import org.battleplugins.api.message.ClickAction;
import org.battleplugins.api.message.HoverAction;
import org.battleplugins.api.message.Message;
import org.battleplugins.api.message.MessageStyle;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.recap.DamageInfo;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.util.TrackerUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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

    private Map<String, Collection<String>> itemMessages;
    private Map<String, Collection<String>> entityMessages;
    private Map<String, Collection<String>> causeMessages;

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
        this.enabled = config.getNode("messages.enabled").getValue(true);
        this.prefix = MessageController.colorChat(config.getNode("prefix").getValue("[Tracker]"));
        this.defaultMessagesOverriden = config.getNode("options.overrideDefaultMessages").getValue(true);
        this.hoverMessagesEnabled = config.getNode("options.useHoverMessages").getValue(true);
        this.clickMessagesEnabled = config.getNode("options.useClickMessages").getValue(true);

        this.hoverContent = config.getNode("options.hoverContent").getValue("all");
        this.clickContent = config.getNode("options.clickContent").getValue("all");

        this.msgRadius = config.getNode("options.msgRadius").getValue(0);

        this.entityMessages = new HashMap<>();
        for (String str : config.getNode("messages.entityDeaths").getCollectionValue(String.class)) {
            entityMessages.put(str, config.getNode("messages.entityDeaths." + str).getCollectionValue(String.class));
        }

        this.causeMessages = new HashMap<>();
        for (String str : config.getNode("messages.causeDeaths").getCollectionValue(String.class)) {
            causeMessages.put(str, config.getNode("messages.causeDeaths." + str).getCollectionValue(String.class));
        }

        this.itemMessages = new HashMap<>();
        for (String str : config.getNode("messages").getCollectionValue(String.class)) {
            // Item messages are just defined right in the messages section, so check for other sections first
            if (str.equalsIgnoreCase("entityDeaths"))
                continue;

            if (str.equalsIgnoreCase("causeDeaths"))
                continue;

            if (str.equalsIgnoreCase("enabled"))
                continue;

            itemMessages.put(str, config.getNode("messages." + str).getCollectionValue(String.class));
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

        // At this point they want these specific messages disabled
        if (itemMessages == null)
            return;

        List<String> items = new ArrayList<>(itemMessages.get(itemName));
        if (items.isEmpty())
            items =  new ArrayList<>(itemMessages.get("unknown")); // item isn't in list

        if (!plugin.getServer().getPlayer(killedName).isPresent())
            return;

        Player killed = plugin.getServer().getPlayer(killedName).get();
        String message = items.get(ThreadLocalRandom.current().nextInt(items.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        Message.Builder messageBuilder = Message.builder().message(prefix + message);
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

        // At this point they want these specific messages disabled
        if (entityMessages == null)
            return;

        List<String> entities = new ArrayList<>(entityMessages.get(killerName));
        if (entities.isEmpty())
            entities = new ArrayList<>(entityMessages.get("unknown")); // entity isn't in list


        if (!plugin.getServer().getPlayer(killedName).isPresent())
            return;

        Player killed = plugin.getServer().getPlayer(killedName).get();
        String message = entities.get(ThreadLocalRandom.current().nextInt(entities.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        Message.Builder messageBuilder = Message.builder().message(prefix + message);
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

        // At this point they want these specific messages disabled
        if (causeMessages == null)
            return;

        List<String> causes = new ArrayList<>(causeMessages.get(killedName));
        if (causes.isEmpty())
            causes = new ArrayList<>(causeMessages.get("unknown")); // cause isn't in list

        if (!plugin.getServer().getPlayer(killedName).isPresent())
            return;

        Player killed = plugin.getServer().getPlayer(killedName).get();
        String message = causes.get(ThreadLocalRandom.current().nextInt(causes.size()));
        message = replacePlaceholders(message, killerName, killedName, itemName, 0);
        message = MessageController.colorChat(message);

        Message.Builder messageBuilder = Message.builder().message(prefix + message);
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

    private void attachHoverEvent(Message.Builder messageBuilder, Player player) {
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

        messageBuilder.hoverAction(HoverAction.SHOW_TEXT);
        messageBuilder.hoverMessage(hoverMessage);
    }

    private void attachClickEvent(Message.Builder messageBuilder, Player player) {
        if (!clickMessagesEnabled || clickContent.equalsIgnoreCase("none"))
            return;

        messageBuilder.clickAction(ClickAction.RUN_COMMAND);
        messageBuilder.clickValue("/" + tracker.getName() + " recap " + player.getName());
    }

    private void sendDeathMessage(Player source, Message message) {
        if (msgRadius == 0) {
            plugin.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        } else {
            for (Entity entity : source.getNearbyEntities(msgRadius, msgRadius, msgRadius)) {
                if (!(entity instanceof Player))
                    continue;

                Player radiusPlayer = (Player) entity;
                radiusPlayer.sendMessage(message);
            }
        }
    }

    private String getRecapHoverMessage(Recap recap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        StringBuilder hoverMessage = new StringBuilder(MessageStyle.GOLD + "Damage Recap:");

        // Obtains last 10 damages dealt
        List<DamageInfo> damageInfos = recap.getLastDamages().stream().sorted((recap1, recap2) -> (int) (recap2.getLogTime() - recap1.getLogTime())).collect(Collectors.toList());
        int max = damageInfos.size();
        if (max > 10)
            max = 10;

        for (int i = damageInfos.size() - max; i < damageInfos.size(); i++) {
            DamageInfo damageInfo = damageInfos.get(i);
            hoverMessage.append("\n" + MessageStyle.RED + " ♥ -")
                    .append(decimalFormat.format(damageInfo.getDamage() / 2))
                    .append(" ")
                    .append(MessageStyle.YELLOW)
                    .append(MessageStyle.AQUA)
                    .append(TrackerUtil.capitalizeFirst(damageInfo.getCause().replace("_", " "))
            );
        }

        return hoverMessage.toString();
    }

    private String getArmorHoverMessage(Player player) {
        return MessageStyle.GOLD + "Armor:"
                + "\n" + MessageStyle.GRAY + " Helmet: " + MessageStyle.YELLOW + player.getInventory().getHelmet().map(this::formatItemName).orElse("None")
                + "\n" + MessageStyle.GRAY + " Chestplate: " + MessageStyle.YELLOW + player.getInventory().getChestplate().map(this::formatItemName).orElse("None")
                + "\n" + MessageStyle.GRAY + " Leggings: " + MessageStyle.YELLOW + player.getInventory().getLeggings().map(this::formatItemName).orElse("None")
                + "\n" + MessageStyle.GRAY + " Boots: " + MessageStyle.YELLOW + player.getInventory().getBoots().map(this::formatItemName).orElse("None");
    }

    private String formatItemName(ItemStack itemStack) {
        if (itemStack.getValue(DisplayNameComponent.class).isPresent()) {
            return itemStack.getValue(DisplayNameComponent.class).get();
        }
        return WordUtils.capitalize(itemStack.getType().getIdentifier().getKey().replace("_", " "));
    }
}
