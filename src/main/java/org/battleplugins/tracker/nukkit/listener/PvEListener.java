package org.battleplugins.tracker.nukkit.listener;

import lombok.AllArgsConstructor;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntityTameable;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerDeathEvent;

import mc.alk.mc.MCPlayer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.tracking.recap.DamageInfo;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.tracking.stat.StatTypes;
import org.battleplugins.tracker.tracking.stat.record.DummyRecord;
import org.battleplugins.tracker.tracking.stat.record.Record;
import org.battleplugins.tracker.util.TrackerUtil;

import java.util.UUID;

/**
 * Main listener for PvE tracking in Nukkit.
 *
 * @author Redned
 */
@AllArgsConstructor
public class PvEListener implements Listener {

    private BattleTracker plugin;

    /**
     * Event called when a player dies
     *
     * @param event the event being called
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();

        String type = "causeDeaths";
        String killer = "unknown";

        EntityDamageEvent lastDamageCause = killed.getLastDamageCause();
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            if (damager instanceof Player)
                return;

            type = "entityDeaths";
            killer = TrackerUtil.getFormattedEntityName(damager.getSaveId(), false).toLowerCase().replace(" ", "");

            if (damager instanceof EntityProjectile) {
                EntityProjectile proj = (EntityProjectile) damager;
                if (proj.shootingEntity instanceof Player)
                    return;

                killer = TrackerUtil.getFormattedEntityName(damager.getSaveId(), false).toLowerCase().replace(" ", "");
            }

            if (damager instanceof EntityTameable && ((EntityTameable) damager).isTamed())
                return; // only players can tame animals

        } else {
            killer = lastDamageCause.getCause().name().toLowerCase().replace("_", "");
        }

        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getOrCreateRecord(plugin.getPlatform().getOfflinePlayer(killed.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatTypes.DEATHS, plugin.getPlatform().getOfflinePlayer(killed.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer);
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(fakeRecord, record, false);

        if (pveTracker.getDeathMessageManager().isDefaultMessagesOverriden())
            event.setDeathMessage("");

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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity killed = event.getEntity();
        if (killed instanceof Player)
            return;

        if (!(killed.getLastDamageCause() instanceof EntityDamageByEntityEvent))
            return;

        EntityDamageByEntityEvent lastDamageCause = (EntityDamageByEntityEvent) killed.getLastDamageCause();
        if (!(lastDamageCause.getDamager() instanceof Player))
            return;

        Player killer = (Player) lastDamageCause.getDamager();
        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getOrCreateRecord(plugin.getPlatform().getOfflinePlayer(killer.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatTypes.KILLS, plugin.getPlatform().getOfflinePlayer(killer.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killed.getSaveId().toLowerCase());
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(record, fakeRecord, false);
    }

    /**
     * Event called when a player takes damage
     *
     * @param event the event being called
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        MCPlayer player = plugin.getPlatform().getPlayer(event.getEntity().getName());
        TrackerInterface pveTracker = plugin.getTrackerManager().getPvEInterface();

        RecapManager recapManager = pveTracker.getRecapManager();
        Recap recap = recapManager.getDeathRecaps().computeIfAbsent(player.getName(), (value) -> new Recap(player));
        if (recap.isVisible()) {
            recap = recapManager.getDeathRecaps().compute(player.getName(), (key, value) -> new Recap(player));
        }

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            recap.getLastDamages().add(new DamageInfo(damageByEntityEvent.getDamager().getName(), (double) event.getDamage()));
        } else {
            recap.getLastDamages().add(new DamageInfo(event.getCause().name().toLowerCase(), (double) event.getDamage()));
        }
    }
}

