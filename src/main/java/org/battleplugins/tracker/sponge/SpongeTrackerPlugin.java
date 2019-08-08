package org.battleplugins.tracker.sponge;

import mc.alk.battlecore.sponge.SpongeBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerInfo;
import org.battleplugins.tracker.sponge.listener.PvPListener;
import org.battleplugins.tracker.sponge.listener.TrackerListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

/**
 * Main class for BattleTracker Sponge.
 *
 * @author Redned
 */
@Plugin(id = "bt", authors = "BattlePlugins", name = TrackerInfo.NAME, version = TrackerInfo.VERSION, description = TrackerInfo.DESCRIPTION, url = TrackerInfo.URL)
public class SpongeTrackerPlugin extends SpongeBattlePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        BattleTracker tracker = new BattleTracker(this);
        BattleTracker.setInstance(tracker);

        Sponge.getEventManager().registerListeners(this, new PvPListener(tracker));
        Sponge.getEventManager().registerListeners(this, new TrackerListener(tracker));
    }

    @Override
    public void onDisable() {

    }
}
