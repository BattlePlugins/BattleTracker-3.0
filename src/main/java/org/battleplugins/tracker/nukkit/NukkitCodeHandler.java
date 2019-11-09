package org.battleplugins.tracker.nukkit;

import lombok.AllArgsConstructor;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;

import mc.alk.battlecore.nukkit.platform.BattleNukkitCodeHandler;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.nukkit.listener.TrackerListener;
import org.battleplugins.tracker.nukkit.listener.PvEListener;
import org.battleplugins.tracker.nukkit.listener.PvPListener;

/**
 * Handler for version-dependent Nukkit code.
 *
 * @author Redned
 */
@AllArgsConstructor
public class NukkitCodeHandler extends BattleNukkitCodeHandler {

    private BattleTracker plugin;

    @Override
    public void onEnable() {
        super.onEnable();

        Plugin plugin = (Plugin) this.plugin.getPlatformPlugin();
        if (this.plugin.getTrackerManager().isTrackingPvE()) {
            Server.getInstance().getPluginManager().registerEvents(new PvEListener(this.plugin), plugin);
        }

        if (this.plugin.getTrackerManager().isTrackingPvP()) {
            Server.getInstance().getPluginManager().registerEvents(new PvPListener(this.plugin), plugin);
        }

        Server.getInstance().getPluginManager().registerEvents(new TrackerListener(this.plugin), plugin);
    }
}
