package org.battleplugins.tracker.nukkit;

import cn.nukkit.plugin.service.ServicePriority;
import mc.alk.battlecore.nukkit.NukkitBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;

/**
 * Main class for BattleTracker Nukkit.
 *
 * @author Redned
 */
public class NukkitTrackerPlugin extends NukkitBattlePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        BattleTracker.setInstance(new BattleTracker(this));
        // Register the tracker manager into the service provider API
        getServer().getServiceManager().register(TrackerManager.class, BattleTracker.getInstance().getTrackerManager(), this, ServicePriority.NORMAL);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
