package org.battleplugins.tracker.sponge.listener;

import mc.alk.mc.MCPlatform;
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
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

/**
 * Main listener for PvE tracking in Sponge.
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
        Record record = pveTracker.getRecord(MCPlatform.getOfflinePlayer(killed.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.DEATHS, MCPlatform.getOfflinePlayer(killed.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer);
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(fakeRecord, record, false);

        if (pveTracker.getMessageManager().shouldOverrideBukkitMessages())
            event.setMessage(Text.of(""));

        if (type.equals("entityDeaths")) {
            pveTracker.getMessageManager().sendEntityMessage(killer, killed.getName(), "air");
        } else {
            pveTracker.getMessageManager().sendCauseMessage(killer, killed.getName(), "air");
        }
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
        Record record = pveTracker.getRecord(MCPlatform.getOfflinePlayer(killer.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.KILLS, MCPlatform.getOfflinePlayer(killer.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer.getType().getName().toLowerCase());
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(record, fakeRecord, false);
    }
}