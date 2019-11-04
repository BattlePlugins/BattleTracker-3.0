package org.battleplugins.tracker.sponge.listener;

import lombok.AllArgsConstructor;

import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.block.MCSign;
import mc.alk.sponge.block.SpongeSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.util.SignUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;

/**
 * Miscellaneous listener for BattleTracker in Sponge.
 *
 * @author Redned
 */
@AllArgsConstructor
public class TrackerListener {

    private BattleTracker plugin;

    /**
     * Event called when player joins
     *
     * @param event the event being called
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        TrackerInterface pvpInterface = plugin.getTrackerManager().getPvPInterface();
        TrackerInterface pveInterface = plugin.getTrackerManager().getPvEInterface();

        MCOfflinePlayer offlinePlayer = plugin.getPlatform().getOfflinePlayer(event.getTargetEntity().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && plugin.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && plugin.getTrackerManager().isTrackingPvE()) {
            pveInterface.createNewRecord(offlinePlayer);
        }
    }

    /**
     * Event called when player quits
     *
     * @param event the event being called
     */
    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        for (Map.Entry<String, TrackerInterface> interfaces : plugin.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(plugin.getPlatform().getOfflinePlayer(event.getTargetEntity().getUniqueId()));
        }

        for (TrackerInterface trackerInterface : plugin.getTrackerManager().getInterfaces().values()) {
            if (!trackerInterface.getRecapManager().getDeathRecaps().containsKey(event.getTargetEntity().getName()))
                continue;

            trackerInterface.getRecapManager().getDeathRecaps().remove(event.getTargetEntity().getName());
        }
    }

    /**
     * Event called when a sign is changed
     *
     * @param event the event being called
     */
    @Listener
    public void onSignChange(ChangeSignEvent event) {
        if (!(event.getSource() instanceof Player))
            return;

        Optional<Player> source = event.getCause().first(Player.class);
        if (!source.isPresent())
            return;

        Player spongePlayer = source.get();
        String[] lines = new String[event.getText().lines().size()];
        for(int i = 0; i < event.getText().lines().size(); ++i) {
            lines[i] = Text.of(event.getText().get(i)).toPlain();
        }

        MCPlayer player = plugin.getPlatform().getPlayer(source.get().getName());
        MCSign sign = new SpongeSign(event.getTargetTile());
        if (!SignUtil.isLeaderboardSign(lines))
            return;

        if (!spongePlayer.hasPermission("battletracker.sign")) {
            event.setCancelled(true); // cancel anyway

            Entity itemEntity = event.getTargetTile().getLocation().getExtent().createEntity(EntityTypes.ITEM, event.getTargetTile().getLocation().getPosition());
            ItemStack itemStack = ItemStack.builder().itemType(event.getTargetTile().getBlock().getType().getItem().get()).quantity(1).build();

            Item item = (Item) itemEntity;
            item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());

            spongePlayer.getLocation().getExtent().setBlockType(event.getTargetTile().getLocation().getPosition().toInt(), BlockTypes.AIR);
            spongePlayer.getLocation().getExtent().spawnEntity(item);
            player.sendMessage(plugin.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(lines);
        String trackerName = SignUtil.getTrackerName(lines);

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        plugin.getSignManager().addSign(leaderboardSign);

        plugin.getPlatform().scheduleSyncTask(plugin, () -> {
            MCSign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockAt(sign.getLocation()), MCSign.class);
            plugin.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(plugin.getMessageManager().getFormattedMessage("createdNewSign"));
    }

    /**
     * Event called when a player clicks in a recap inventory
     *
     * @param event the event being called
     */
    @Listener
    public void onInventoryClick(ClickInventoryEvent event) {
        if (event.getTargetInventory().getName().get().endsWith("'s Recap"))
            event.setCancelled(true);
    }
}
