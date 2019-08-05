package org.battleplugins.tracker.bukkit;

import mc.alk.bukkit.plugin.BukkitPlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.bukkit.plugin.ServicePriority;

/**
 * Main class for BattleTracker Bukkit.
 *
 * @author Redned
 */
public class BukkitTrackerPlugin extends BukkitPlugin {

    @Override
    public void onEnable() {
        BattleTracker.setInstance(new BattleTracker(this));
        // Register the tracker manager into the service provider API
        getServer().getServicesManager().register(TrackerManager.class, BattleTracker.getInstance().getTrackerManager(), this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {

    }
}
