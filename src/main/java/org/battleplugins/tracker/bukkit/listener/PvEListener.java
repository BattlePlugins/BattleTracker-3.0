package org.battleplugins.tracker.bukkit.listener;

import mc.alk.mc.MCServer;
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
        Record record = pveTracker.getRecord(MCServer.getOfflinePlayer(killer.getName()));
        if (record.isTracking())
            pveTracker.incrementValue(StatType.KILLS, MCServer.getOfflinePlayer(killer.getName()));

        Record fakeRecord = new DummyRecord(pveTracker, killed.getType().name().toLowerCase(), 1250);
        tracker.getDefaultCalculator().updateRating(record, fakeRecord, false);
    }
}
