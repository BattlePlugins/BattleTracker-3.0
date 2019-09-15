package org.battleplugins.tracker.bukkit;

import mc.alk.mc.plugin.platform.PlatformCodeHandler;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.bukkit.listener.PvEListener;
import org.battleplugins.tracker.bukkit.listener.PvPListener;
import org.battleplugins.tracker.bukkit.listener.TrackerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/**
 * Handler for version-dependent Bukkit code.
 *
 * @author Redned
 */
public class BukkitCodeHandler extends PlatformCodeHandler {

    private BattleTracker tracker;

    public BukkitCodeHandler(BattleTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onEnable() {
        Plugin plugin = (Plugin) tracker.getPlatformPlugin();
        // Register the tracker manager into the service provider API
        Bukkit.getServer().getServicesManager().register(TrackerManager.class, tracker.getTrackerManager(), plugin, ServicePriority.Normal);

        if (tracker.getTrackerManager().isTrackingPvE()) {
            Bukkit.getServer().getPluginManager().registerEvents(new PvEListener(tracker), plugin);
        }

        if (tracker.getTrackerManager().isTrackingPvP()) {
            Bukkit.getServer().getPluginManager().registerEvents(new PvPListener(tracker), plugin);
        }

        Bukkit.getServer().getPluginManager().registerEvents(new TrackerListener(tracker), plugin);
    }
}
