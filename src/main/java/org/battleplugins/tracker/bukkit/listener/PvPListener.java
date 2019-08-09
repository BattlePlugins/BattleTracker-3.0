package org.battleplugins.tracker.bukkit.listener;

import mc.alk.bukkit.BukkitOfflinePlayer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInterface;
import org.battleplugins.tracker.bukkit.util.CompatUtil;
import org.battleplugins.tracker.stat.StatType;
import org.battleplugins.tracker.stat.record.Record;
import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Main listener for PvP tracking in Bukkit.
 *
 * @author Redned
 */
public class PvPListener implements Listener {

    private BattleTracker tracker;

    public PvPListener(BattleTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Event called when one player is killed by another
     *
     * @param event the event being called
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();

        EntityDamageEvent lastDamageCause = killed.getLastDamageCause();
        Player killer = null;
        ItemStack weapon = null;
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            if (damager instanceof Player) {
                killer = (Player) damager;
                weapon = CompatUtil.getItemInMainHand(killer);
            }

            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player) {
                    killer = (Player) damager;
                    weapon = CompatUtil.getItemInMainHand(killer);
                }
            }

            if (damager instanceof Tameable && ((Tameable) damager).isTamed()) {
                AnimalTamer owner = ((Tameable) damager).getOwner();
                if (owner instanceof Player) {
                    killer = (Player) damager;
                    // Use a bone to show the case was a wolf
                    ItemStack bone = new ItemStack(Material.BONE);
                    ItemMeta meta = bone.getItemMeta();
                    meta.setDisplayName(damager.getCustomName() == null ? "Wolf" : damager.getCustomName());
                    bone.setItemMeta(meta);
                    weapon = bone;
                }
            }
        }

        if (killer == null)
            return;

        // Check the killers world just incase for some reason the
        // killed player was teleported to another world
        if (tracker.getPvPConfig().getStringList("ignoredWorlds").contains(killer.getWorld().getName()))
            return;

        updateStats(killed, killer);

        // TODO: Add death messages
    }

    public void updateStats(Player killed, Player killer) {
        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();
        Record killerRecord = pvpTracker.getRecord(new BukkitOfflinePlayer(killer));
        Record killedRecord = pvpTracker.getRecord(new BukkitOfflinePlayer(killed));

        if (killerRecord.isTracking())
            pvpTracker.incrementValue(StatType.KILLS, new BukkitOfflinePlayer(killer));

        if (killedRecord.isTracking())
            pvpTracker.incrementValue(StatType.DEATHS, new BukkitOfflinePlayer(killed));

        pvpTracker.updateRating(new BukkitOfflinePlayer(killer), new BukkitOfflinePlayer(killed), false);
    }
}
