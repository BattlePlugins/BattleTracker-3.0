package org.battleplugins.tracker.bukkit.listener;

import lombok.AllArgsConstructor;

import org.battleplugins.api.bukkit.world.block.entity.BukkitSign;
import org.battleplugins.api.entity.living.player.Player;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.sign.LeaderboardSign;
import org.battleplugins.tracker.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Miscellaneous listener for BattleTracker in Bukkit.
 *
 * @author Redned
 */
@AllArgsConstructor
public class TrackerListener implements Listener {

    private BattleTracker plugin;

    /**
     * Event called when a sign is changed
     *
     * @param event the event being called
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = plugin.getServer().getPlayer(event.getPlayer().getName()).get();
        org.battleplugins.api.world.block.entity.Sign sign = new BukkitSign((Sign) event.getBlock().getState());
        if (!SignUtil.isLeaderboardSign(event.getLines()))
            return;

        if (!event.getPlayer().hasPermission("battletracker.sign")) {
            event.setCancelled(true); // cancel anyway
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType(), 1));
            player.sendMessage(plugin.getMessageManager().getFormattedMessage("cantCreateSign"));
            return;
        }

        String statType = SignUtil.getStatType(event.getLines());
        String trackerName = SignUtil.getTrackerName(event.getLines());

        LeaderboardSign leaderboardSign = new LeaderboardSign(sign.getLocation(), statType, trackerName);
        plugin.getSignManager().addSign(leaderboardSign);

        plugin.getPlatform().scheduleSyncTask(plugin, () -> {
            org.battleplugins.api.world.block.entity.Sign reobtainedSign = sign.getWorld().toType(sign.getWorld().getBlockEntityAt(sign.getLocation()).get(), org.battleplugins.api.world.block.entity.Sign.class);
            plugin.getSignManager().refreshSignContent(reobtainedSign);
        }, 2000);
        player.sendMessage(plugin.getMessageManager().getFormattedMessage("createdNewSign"));
    }

    /**
     * Event called when a player clicks in a recap inventory
     *
     * @param event the event being called
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("'s Recap"))
            event.setCancelled(true);
    }
}
