package org.battleplugins.tracker.nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.service.ServicePriority;
import mc.alk.mc.plugin.platform.PlatformCodeHandler;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.nukkit.listener.TrackerListener;
import org.battleplugins.tracker.nukkit.listener.PvEListener;
import org.battleplugins.tracker.nukkit.listener.PvPListener;

/**
 * Handler for version-dependent Nukkit code.
 *
 * @author Redned
 */
public class NukkitCodeHandler extends PlatformCodeHandler {

    private BattleTracker tracker;

    public NukkitCodeHandler(BattleTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onEnable() {
        Plugin plugin = (Plugin) tracker.getPlatformPlugin();

        // Register the tracker manager into the service provider API
        Server.getInstance().getServiceManager().register(TrackerManager.class, tracker.getTrackerManager(), plugin, ServicePriority.NORMAL);

        if (tracker.getTrackerManager().isTrackingPvE()) {
            Server.getInstance().getPluginManager().registerEvents(new PvEListener(tracker), plugin);
        }

        if (tracker.getTrackerManager().isTrackingPvP()) {
            Server.getInstance().getPluginManager().registerEvents(new PvPListener(tracker), plugin);
        }

        Server.getInstance().getPluginManager().registerEvents(new TrackerListener(tracker), plugin);
    }
}
