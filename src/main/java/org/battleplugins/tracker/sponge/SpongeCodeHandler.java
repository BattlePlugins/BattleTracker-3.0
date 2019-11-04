package org.battleplugins.tracker.sponge;

import lombok.AllArgsConstructor;

import mc.alk.mc.plugin.platform.PlatformCodeHandler;
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
@AllArgsConstructor
public class SpongeCodeHandler extends PlatformCodeHandler {

    private BattleTracker plugin;

    @Override
    public void onEnable() {
        if (plugin.getTrackerManager().isTrackingPvE()) {
            Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new PvEListener(plugin));
        }

        if (plugin.getTrackerManager().isTrackingPvP()) {
            Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new PvPListener(plugin));
        }

        Sponge.getEventManager().registerListeners(plugin.getPlatformPlugin(), new TrackerListener(plugin));
    }
}
