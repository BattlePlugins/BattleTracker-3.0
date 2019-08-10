package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCServer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.DummyRecord;
import org.battleplugins.tracker.stat.record.Record;
import org.battleplugins.tracker.util.Util;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;

import java.util.Optional;

/**
 * Main listener for PvE tracking in Nukkit.
 *
 * @author Redned
 */
public class PvEListener {

    private BattleTracker tracker;

    public PvEListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when a player dies
     *
     * @param event the event being called
     */
    @Listener
    public void onDeath(DestructEntityEvent.Death event) {
        if (!(event.getTargetEntity() instanceof org.spongepowered.api.entity.living.player.Player))
            return;

        Player killed = (Player) event.getTargetEntity();

        String type = "causeDeaths";
        String killer = "unknown";

        Optional<EntityDamageSource> source = event.getCause().first(EntityDamageSource.class);
        if (source.isPresent()) {
            Entity damager = source.get().getSource();
            if (damager instanceof Player)
                return;

            type = "entityDeaths";
            killer = Util.getFormattedEntityName(damager.getType().getName(), false).toLowerCase().replace(" ", "");

            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player)
                    return;

                killer = Util.getFormattedEntityName(damager.getType().getName(), false).toLowerCase().replace(" ", "");
            }

            // Sponge has no support for tameable entities..
        } else {
            Optional<BlockDamageSource> blockSource = event.getCause().first(BlockDamageSource.class);
            if (blockSource.isPresent()) {
                killer = blockSource.get().getType().toString().toLowerCase().replace("_", "");
            }
        }

        TrackerInterface pveTracker = tracker.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getRecord(MCServer.getOfflinePlayer(killed.getName()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.DEATHS, MCServer.getOfflinePlayer(killed.getName()));

        Record fakeRecord = new DummyRecord(pveTracker, killer, 1250);
        tracker.getDefaultCalculator().updateRating(fakeRecord, record, false);

        // TODO: Add death messages here
    }

    /**
     * Event called when a player kills an entity
     *
     * @param event the event being called
     */
    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
        Entity killed = event.getTargetEntity();
        if (killed instanceof Player)
            return;

        Optional<EntityDamageSource> opSource = event.getCause().first(EntityDamageSource.class);
        if (!opSource.isPresent())
            return;

        EntityDamageSource source = opSource.get();
        if (!(source.getSource() instanceof Player))
            return;

        Player killer = (Player) source.getSource();
        TrackerInterface pveTracker = tracker.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getRecord(MCServer.getOfflinePlayer(killer.getName()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.KILLS, MCServer.getOfflinePlayer(killer.getName()));

        Record fakeRecord = new DummyRecord(pveTracker, killed.getType().getName().toLowerCase(), 1250);
        tracker.getDefaultCalculator().updateRating(record, fakeRecord, false);
    }
}