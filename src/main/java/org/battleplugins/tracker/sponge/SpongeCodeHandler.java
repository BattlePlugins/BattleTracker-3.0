package org.battleplugins.tracker.sponge;

import mc.alk.battlecore.sponge.platform.BattleSpongeCodeHandler;

import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.sponge.listener.PvEListener;
import org.battleplugins.tracker.sponge.listener.PvPListener;
import org.battleplugins.tracker.sponge.listener.TrackerListener;
import org.spongepowered.api.Sponge;

/**
 * Handler for version-dependent Sponge code.
 *
 * @author Redned
 */
public class SpongeCodeHandler extends BattleSpongeCodeHandler {

    private BattleTracker plugin;

    public SpongeCodeHandler(BattleTracker plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (plugin.getTrackerManager().isTrackingPvE()) {
            Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new PvEListener(plugin));
        }

        if (plugin.getTrackerManager().isTrackingPvP()) {
            Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new PvPListener(plugin));
        }

        Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new TrackerListener(plugin));
    }
}
