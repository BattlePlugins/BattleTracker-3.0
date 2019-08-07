package org.battleplugins.tracker.bukkit;

import mc.alk.battlecore.bukkit.BukkitBattlePlugin;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.TrackerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.ServicePriority;

import java.lang.reflect.Field;

/**
 * Main class for BattleTracker Bukkit.
 *
 * @author Redned
 */
public class BukkitTrackerPlugin extends BukkitBattlePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        BattleTracker.setInstance(new BattleTracker(this));
        // Register the tracker manager into the service provider API
        getServer().getServicesManager().register(TrackerManager.class, BattleTracker.getInstance().getTrackerManager(), this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static CommandMap getCommandMap() {
        Field field;
        try {
            field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
