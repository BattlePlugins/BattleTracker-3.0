package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCPlatform;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * Main listener for PvP tracking in Sponge.
 *
 * @author Redned
 */
public class PvPListener {

    private BattleTracker tracker;

    public PvPListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when one player is killed by another
     *
     * @param event the event being called
     */
    @Listener
    public void onDeath(DestructEntityEvent.Death event) {
        if (!(event.getTargetEntity() instanceof Player))
            return;

        Optional<EntityDamageSource> source = event.getCause().first(EntityDamageSource.class);
        if (!source.isPresent())
            return; // did not die from pvp

        Player killed = (Player) event.getTargetEntity();

        Player killer = null;
        ItemStack weapon = null;
        Entity damager = source.get().getSource();
        if (damager instanceof Player) {
            killer = (Player) damager;
            weapon = killer.getItemInHand(HandTypes.MAIN_HAND).get();
        }

        if (damager instanceof Projectile) {
            Projectile proj = (Projectile) damager;
            if (proj.getShooter() instanceof Player) {
                killer = (Player) damager;
                weapon = killer.getItemInHand(HandTypes.MAIN_HAND).get();
            }
        }

        // Sponge has no support for tameable entities..

        if (killer == null)
            return;

        // Check the killers world just incase for some reason the
        // killed player was teleported to another world
        if (tracker.getPvPConfig().getStringList("ignoredWorlds").contains(killer.getWorld().getName()))
            return;

        updateStats(killed, killer);

        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();
        if (pvpTracker.getMessageManager().shouldOverrideDefaultMessages())
            event.setMessage(Text.of(""));

        pvpTracker.getMessageManager().sendItemMessage(killer.getName(), killed.getName(), weapon.getType().getName().toLowerCase());
    }

    public void updateStats(Player killed, Player killer) {
        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();
        Record killerRecord = pvpTracker.getRecord(MCPlatform.getOfflinePlayer(killer.getUniqueId()));
        Record killedRecord = pvpTracker.getRecord(MCPlatform.getOfflinePlayer(killed.getUniqueId()));

        if (killerRecord.isTracking())
            pvpTracker.incrementValue(StatType.KILLS,MCPlatform.getOfflinePlayer(killer.getUniqueId()));

        if (killedRecord.isTracking())
            pvpTracker.incrementValue(StatType.DEATHS, MCPlatform.getOfflinePlayer(killed.getUniqueId()));

        pvpTracker.updateRating(MCPlatform.getOfflinePlayer(killer.getUniqueId()), MCPlatform.getOfflinePlayer(killed.getUniqueId()), false);
    }
}
