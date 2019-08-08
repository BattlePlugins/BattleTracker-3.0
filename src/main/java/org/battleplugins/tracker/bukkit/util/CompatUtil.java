package org.battleplugins.tracker.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Utility methods for compatibility across different
 * Bukkit versions.
 *
 * @author Redned
 */
public class CompatUtil {

    /**
     * Returns the item in main hand. Uses getItemInHand() in
     * versions <1.9
     *
     * @param player the player to get the hand item for
     * @return the item in the main hand
     */
    public static ItemStack getItemInMainHand(Player player) {
        try {
            // 1.9+
            return player.getInventory().getItemInMainHand();
        } catch (Throwable ex) {
            // pre 1.9
            return player.getItemInHand();
        }
    }
}
