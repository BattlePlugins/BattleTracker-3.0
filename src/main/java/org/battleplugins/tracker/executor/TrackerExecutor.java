package org.battleplugins.tracker.executor;

import mc.alk.battlecore.controllers.MessageController;
import mc.alk.battlecore.executor.CustomCommandExecutor;
import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.command.MCCommandSender;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.message.MessageManager;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatType;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.Util;

import java.util.Map;

/**
 * Main executor for trackers.
 *
 * @author Redned
 */
public class TrackerExecutor extends CustomCommandExecutor {

    private BattleTracker tracker;
    private String interfaceName;

    public TrackerExecutor(BattleTracker tracker, String interfaceName) {
        this.tracker = tracker;
        this.interfaceName = interfaceName;
    }

    @MCCommand(cmds = "top")
    public void topCommand(MCCommandSender sender) {
        topCommand(sender, 5);
    }

    @MCCommand(cmds = "top")
    public void topCommand(MCCommandSender sender, int amount) {
        MessageManager messageManager = tracker.getMessageManager();
        sender.sendMessage(MessageController.colorChat(messageManager.getMessage("leaderboardHeader").replace("%tracker%", interfaceName)));
        Map<Record, Float> sortedRecords = Util.getSortedRecords(tracker.getTrackerManager().getInterface(interfaceName), amount);

        int i = 1;
        for (Map.Entry<Record, Float> recordEntry : sortedRecords.entrySet()) {
            String message = messageManager.getMessage("leaderboardText");
            message = message.replace("%ranking%", String.valueOf(i));
            message = message.replace("%rating%", String.valueOf((int) recordEntry.getKey().getRating()));
            message = message.replace("%kills%", String.valueOf((int) recordEntry.getKey().getStat(StatType.KILLS)));
            message = message.replace("%deaths%", String.valueOf((int) recordEntry.getKey().getStat(StatType.DEATHS)));
            message = message.replace("%player_name%", recordEntry.getKey().getName());
            message = message.replace("%tracker%", interfaceName);
            sender.sendMessage(MessageController.colorChat(message));

            // limit at 100 to prevent lag and spam
            if (i >= amount || i >= 100)
                break;

            i++;
        }
    }

    @MCCommand(cmds = "rank")
    public void rankCommand(MCCommandSender sender, MCOfflinePlayer player) {
        MessageManager messageManager = tracker.getMessageManager();
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        Record record = trackerInterface.getRecord(player);
        if (record == null) {
            sender.sendMessage(messageManager.getFormattedMessage(player, "recordNotFound"));
            return;
        }

        // Replace this seperately since everything else is parsed as an int, only temporary for now
        // TODO: Address this later
        String message = messageManager.getFormattedMessage(player, "rankingText");
        message = message.replace("%kd_ratio%", String.valueOf(record.getStat(StatType.KD_RATIO)));

        for (StatType type : StatType.values()) {
            message = message.replace("%" + type.getInternalName() + "%", String.valueOf((int) record.getStat(type)));
        }

        sender.sendMessage(message);
    }

    @MCCommand(cmds = "reset", perm = "battletracker.reset")
    public void resetCommand(MCCommandSender sender, MCOfflinePlayer player) {
        MessageManager messageManager = tracker.getMessageManager();
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        if (!trackerInterface.hasRecord(player)) {
            sender.sendMessage(messageManager.getFormattedMessage(player, "recordNotFound"));
            return;
        }

        trackerInterface.createNewRecord(player);
        sender.sendMessage(messageManager.getFormattedMessage(player, "recordsReset").replace("%tracker%", trackerInterface.getName()));
    }

    @MCCommand(cmds = "set", perm = "battletracker.set")
    public void setCommand(MCCommandSender sender, MCOfflinePlayer player, String statType, float value) {
        MessageManager messageManager = tracker.getMessageManager();
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        Record record = trackerInterface.getRecord(player);
        if (record == null) {
            sender.sendMessage(messageManager.getFormattedMessage(player, "recordNotFound"));
            return;
        }

        if (!record.getStats().containsKey(statType.toLowerCase())) {
            sender.sendMessage(messageManager.getFormattedMessage("statNotInTracker"));
            return;
        }

        trackerInterface.setValue(statType, value, player);
        sender.sendMessage(messageManager.getFormattedMessage(player, "setStatValue").replace("%stat%", statType.toLowerCase()).replace("%value%", String.valueOf(value)));
    }

    @MCCommand(cmds = "recap", perm = "battletracker.recap")
    public void recapCommand(MCPlayer player, String name) {
        MessageManager messageManager = tracker.getMessageManager();
        TrackerInterface trackerInterface = tracker.getTrackerManager().getInterface(interfaceName);
        RecapManager recapManager = trackerInterface.getRecapManager();

        if (!recapManager.getDeathRecaps().containsKey(name)) {
            player.sendMessage(messageManager.getFormattedMessage("noRecapForPlayer"));
            return;
        }

        Recap recap = recapManager.getDeathRecaps().get(name);
        if (!recap.isVisible()) {
            player.sendMessage(messageManager.getFormattedMessage("noRecapForPlayer"));
            return;
        }

        switch (trackerInterface.getDeathMessageManager().getClickContent()) {
            case "armor":
                trackerInterface.getRecapManager().sendArmorRecap(player, recap);
                break;
            case "inventory":
                trackerInterface.getRecapManager().sendInventoryRecap(player, recap);
        }
    }
}
