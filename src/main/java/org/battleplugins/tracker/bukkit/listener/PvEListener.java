package org.battleplugins.tracker.bukkit.listener;

import mc.alk.mc.MCPlatform;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.DummyRecord;
import org.battleplugins.tracker.stat.record.Record;
import org.battleplugins.tracker.util.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Main listener for PvE tracking in Bukkit.
 *
 * @author Redned
 */
public class PvEListener implements Listener {

    private BattleTracker tracker;

    public PvEListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

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
            killer = Util.getFormattedEntityName(damager.getType().name(), false).toLowerCase().replace(" ", "");

            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player)
                    return;

                killer = Util.getFormattedEntityName(damager.getType().name(), false).toLowerCase().replace(" ", "");
            }

            if (damager instanceof Tameable && ((Tameable) damager).isTamed())
                return; // only players can tame animals

        } else {
            killer = lastDamageCause.getCause().name().toLowerCase().replace("_", "");
        }

        TrackerInterface pveTracker = tracker.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getRecord(MCPlatform.getOfflinePlayer(killed.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.DEATHS, MCPlatform.getOfflinePlayer(killed.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer);
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(fakeRecord, record, false);

        if (pveTracker.getMessageManager().shouldOverrideBukkitMessages())
            event.setDeathMessage(null);

        if (type.equals("entityDeaths")) {
            pveTracker.getMessageManager().sendEntityMessage(killer, killed.getName(), "air", 0);
        } else {
            pveTracker.getMessageManager().sendCauseMessage(killer, killed.getName(), "air", 0);
        }
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
        TrackerInterface pveTracker = tracker.getTrackerManager().getPvEInterface();
        Record record = pveTracker.getRecord(MCPlatform.getOfflinePlayer(killer.getUniqueId()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.KILLS, MCPlatform.getOfflinePlayer(killer.getUniqueId()));

        Record fakeRecord = new DummyRecord(pveTracker, UUID.randomUUID().toString(), killer.getType().name().toLowerCase());
        fakeRecord.setRating(pveTracker.getRatingCalculator().getDefaultRating());
        pveTracker.getRatingCalculator().updateRating(record, fakeRecord, false);
    }
}
