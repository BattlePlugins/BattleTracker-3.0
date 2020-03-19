package org.battleplugins.tracker.sponge.listener;

import lombok.AllArgsConstructor;

import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.recap.DamageInfo;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatTypes;
import org.battleplugins.tracker.tracking.stat.record.DummyRecord;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.TrackerUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

/**
 * Main listener for PvE tracking in Sponge.
 *
 * @author Redned
 */
@AllArgsConstructor
public class PvEListener {

    private BattleTracker plugin;

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
            killer = TrackerUtil.getFormattedEntityName(damager.getType().getName(), false).toLowerCase().replace(" ", "");

            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player)
                    return;

                killer = TrackerUtil.getFormattedEntityName(damager.getType().getName(), false).toLowerCase().replace(" ", "");
            }

            // Sponge has no support for tameable entities..
        } else {
            Optional<BlockDamageSource> blockSource = event.getCause().first(BlockDamageSource.class);
            if (blockSource.isPresent()) {
                killer = blockSource.get().getType().toString().toLowerCase().replace("_", "");
            }
        }

        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getOrCreateRecord(plugin.getServer().getOfflinePlayer(killed.getUniqueId()).get());
        if (record.isTracking())
            pveTracker.incrementValue(StatTypes.DEATHS, plugin.getServer().getOfflinePlayer(killed.getUniqueId()).get());

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer);
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(fakeRecord, record, false);

        if (pveTracker.getDeathMessageManager().isDefaultMessagesOverriden())
            event.setMessageCancelled(true);

        if (type.equals("entityDeaths")) {
            pveTracker.getDeathMessageManager().sendEntityMessage(killer, killed.getName(), "air");
        } else {
            pveTracker.getDeathMessageManager().sendCauseMessage(killer, killed.getName(), "air");
        }

        pveTracker.getRecapManager().getDeathRecaps().get(killed.getName()).setVisible(true);
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
        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getOrCreateRecord(plugin.getServer().getOfflinePlayer(killer.getUniqueId()).get());
        if (record.isTracking())
            pveTracker.incrementValue(StatTypes.KILLS, plugin.getServer().getOfflinePlayer(killer.getUniqueId()).get());

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer.getType().getName().toLowerCase());
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(record, fakeRecord, false);
    }

    /**
     * Event called when a player takes damage
     *
     * @param event the event being called
     */
    @Listener
    public void onEntityDamage(DamageEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player))
            return;

        Player spongePlayer = (Player) event.getTargetEntity();
        org.battleplugins.api.entity.living.player.Player player = plugin.getServer().getPlayer(spongePlayer.getName()).get();
        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();

        RecapManager recapManager = pveTracker.getRecapManager();
        Recap recap = recapManager.getDeathRecaps().computeIfAbsent(player.getName(), (value) -> new Recap(player));
        if (recap.isVisible()) {
            recap = recapManager.getDeathRecaps().compute(player.getName(), (key, value) -> new Recap(player));
        }

        final Recap finalRecap = recap;
        Optional<EntityDamageSource> opEntitySource = event.getCause().first(EntityDamageSource.class);
        Optional<DamageSource> opDamageSource = event.getCause().first(DamageSource.class);

        if (opEntitySource.isPresent()) {
            EntityDamageSource source = opEntitySource.get();
            recap.getLastDamages().add(new DamageInfo(source.getSource().get(Keys.DISPLAY_NAME).orElse(Text.of(source.getSource().getType().getName())).toPlain(), event.getFinalDamage()));
        } else {
            opDamageSource.ifPresent(damageSource -> finalRecap.getLastDamages().add(new DamageInfo(damageSource.getType().getName(), event.getFinalDamage())));
        }
    }
}