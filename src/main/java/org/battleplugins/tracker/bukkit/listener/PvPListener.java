package org.battleplugins.tracker.bukkit.listener;

import mc.alk.mc.MCPlayer;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.tracking.TrackerInterface;
import org.battleplugins.tracker.bukkit.util.CompatUtil;
import org.battleplugins.tracker.tracking.recap.DamageInfo;
import org.battleplugins.tracker.tracking.recap.Recap;
import org.battleplugins.tracker.tracking.recap.RecapManager;
import org.battleplugins.tracker.util.TrackerUtil;
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
                    killer = (Player) proj.getShooter();
                    weapon = CompatUtil.getItemInMainHand(killer);
                }
            }

            if (damager instanceof Tameable && ((Tameable) damager).isTamed()) {
                AnimalTamer owner = ((Tameable) damager).getOwner();
                if (owner instanceof Player) {
                    killer = (Player) owner;
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
        if (tracker.getConfigManager().getPvPConfig().getStringList("ignoredWorlds").contains(killer.getWorld().getName()))
            return;

        TrackerUtil.updatePvPStats(tracker.getPlatform().getOfflinePlayer(killed.getUniqueId()),
                tracker.getPlatform().getOfflinePlayer(killer.getUniqueId()));

        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();
        if (pvpTracker.getDeathMessageManager().shouldOverrideDefaultMessages())
            event.setDeathMessage(null);

        pvpTracker.getDeathMessageManager().sendItemMessage(killer.getName(), killed.getName(), weapon.getType().name().toLowerCase());
        pvpTracker.getRecapManager().getDeathRecaps().get(killed.getName()).setVisible(true);
    }

    /**
     * Event called when a player takes damage from another player
     *
     * @param event the event being called
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(getTrueDamager(event) instanceof Player))
            return;

        MCPlayer player = tracker.getPlatform().getPlayer(event.getEntity().getName());
        TrackerInterface pvpTracker = tracker.getTrackerManager().getPvPInterface();

        RecapManager recapManager = pvpTracker.getRecapManager();
        Recap recap = recapManager.getDeathRecaps().computeIfAbsent(player.getName(), (value) -> new Recap(player));
        if (recap.isVisible()) {
            recap = recapManager.getDeathRecaps().compute(player.getName(), (key, value) -> new Recap(player));
        }

        recap.getLastDamages().add(new DamageInfo(event.getEntity().getName(), event.getDamage()));
    }

    private Entity getTrueDamager(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            Projectile proj = (Projectile) damager;
            if (proj.getShooter() instanceof Entity) {
                return (Entity) proj.getShooter();
            }
        }

        if (damager instanceof Tameable && ((Tameable) damager).isTamed()) {
            AnimalTamer owner = ((Tameable) damager).getOwner();
            if (owner instanceof Entity) {
                return (Entity) owner;
            }
        }

        return damager;
    }
}
