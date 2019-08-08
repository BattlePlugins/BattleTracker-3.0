package org.battleplugins.tracker.nukkit;

import cn.nukkit.plugin.service.ServicePriority;
import mc.alk.battlecore.nukkit.NukkitBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.battleplugins.tracker.nukkit.listener.PvPListener;
import org.battleplugins.tracker.nukkit.listener.TrackerListener;

/**
 * Main class for BattleTracker Nukkit.
 *
 * @author Redned
 */
public class NukkitTrackerPlugin extends NukkitBattlePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        BattleTracker tracker = new BattleTracker(this);
        BattleTracker.setInstance(tracker);
        // Register the tracker manager into the service provider API
        getServer().getServiceManager().register(TrackerManager.class, BattleTracker.getInstance().getTrackerManager(), this, ServicePriority.NORMAL);

        getServer().getPluginManager().registerEvents(new PvPListener(tracker), this);
        getServer().getPluginManager().registerEvents(new TrackerListener(tracker), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
