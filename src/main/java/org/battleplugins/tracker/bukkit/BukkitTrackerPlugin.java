package org.battleplugins.tracker.bukkit;

import mc.alk.battlecore.bukkit.BukkitBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.bukkit.listener.PvPListener;
import org.battleplugins.tracker.bukkit.listener.TrackerListener;
import org.bukkit.plugin.ServicePriority;

/**
 * Main class for BattleTracker Bukkit.
 *
 * @author Redned
 */
public class BukkitTrackerPlugin extends BukkitBattlePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        BattleTracker tracker = new BattleTracker(this);
        BattleTracker.setInstance(tracker);
        // Register the tracker manager into the service provider API
        getServer().getServicesManager().register(TrackerManager.class, BattleTracker.getInstance().getTrackerManager(), this, ServicePriority.Normal);

        getServer().getPluginManager().registerEvents(new PvPListener(tracker), this);
        getServer().getPluginManager().registerEvents(new TrackerListener(tracker), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
