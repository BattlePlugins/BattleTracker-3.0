package org.battleplugins.tracker.bukkit;

import lombok.AllArgsConstructor;

import mc.alk.mc.plugin.platform.PlatformCodeHandler;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.bukkit.listener.PvEListener;
import org.battleplugins.tracker.bukkit.listener.PvPListener;
import org.battleplugins.tracker.bukkit.listener.TrackerListener;
import org.battleplugins.tracker.bukkit.plugins.BTPlaceholderExtension;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Handler for version-dependent Bukkit code.
 *
 * @author Redned
 */
@AllArgsConstructor
public class BukkitCodeHandler extends PlatformCodeHandler {

    private BattleTracker plugin;

    @Override
    public void onEnable() {
        Plugin plugin = (Plugin) this.plugin.getPlatformPlugin();
        if (this.plugin.getTrackerManager().isTrackingPvE()) {
            Bukkit.getServer().getPluginManager().registerEvents(new PvEListener(this.plugin), plugin);
        }

        if (this.plugin.getTrackerManager().isTrackingPvP()) {
            Bukkit.getServer().getPluginManager().registerEvents(new PvPListener(this.plugin), plugin);
        }

        Bukkit.getServer().getPluginManager().registerEvents(new TrackerListener(this.plugin), plugin);

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BTPlaceholderExtension(this.plugin).register();
        }
    }
}
