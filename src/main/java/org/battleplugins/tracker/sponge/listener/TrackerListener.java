package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCOfflinePlayer;
import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.block.MCSign;
import mc.alk.sponge.block.SpongeSign;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
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
public class TrackerListener {

    private BattleTracker tracker;

    public TrackerListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when player joins
     *
     * @param event the event being called
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        TrackerInterface pvpInterface = tracker.getTrackerManager().getPvPInterface();
        TrackerInterface pveInterface = tracker.getTrackerManager().getPvEInterface();

        MCOfflinePlayer offlinePlayer = MCPlatform.getOfflinePlayer(event.getTargetEntity().getUniqueId());
        if (!pvpInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvP()) {
            pvpInterface.createNewRecord(offlinePlayer);
        }

        if (!pveInterface.hasRecord(offlinePlayer) && tracker.getTrackerManager().isTrackingPvE()) {
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
        for (Map.Entry<String, TrackerInterface> interfaces : tracker.getTrackerManager().getInterfaces().entrySet()) {
            interfaces.getValue().save(MCPlatform.getOfflinePlayer(event.getTargetEntity().getUniqueId()));
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

        MCPlayer player = MCPlatform.getPlayer(source.get().getName());
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
            player.sendMessage(tracker.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(lines);
        String trackerName = SignUtil.getTrackerName(lines);

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        tracker.getSignManager().addSign(leaderboardSign);

        MCPlatform.scheduleSyncDelayedTask(tracker, () -> {
            MCSign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockAt(sign.getLocation()), MCSign.class);
            tracker.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(tracker.getMessageManager().getFormattedMessage("createdNewSign"));
    }
}
