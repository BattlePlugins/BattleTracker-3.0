package org.battleplugins.tracker.tracking.recap;

import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.inventory.MCInventory;
import mc.alk.mc.inventory.MCPlayerInventory;
import org.battleplugins.tracker.BattleTracker;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores recap information about a player's death such as:
 * how much damage they've dealt, how much damage they've taken and
 * other miscellaneous information pertaining to their last damage(s).
 *
 * @author Redned
 */
@Getter
public class Recap {

    /**
     * A list of the last damages dealt

     * @return a list of the last damages dealt
     */
    private List<DamageInfo> lastDamages;

    /**
     * The inventory associated with this recap
     *
     * @return the inventory associated with this recap
     */
    private MCInventory inventory;

    /**
     * The player name associated with this recap
     *
     * @return the player name associated with this recap
     */
    private String playerName;

    /**
     * The amount of health the player originally started with
     *
     * @return the amount of health the player originally started with
     */
    private double startingHealth;

    /**
     * The time that the player in this record died
     *
     * @return the time that the player in this record died
     */
    private long deathTime;

    /**
     * If this recap is visible in the recap command
     *
     * @param visible if this recap is visible in the recap command
     * @return if this recap is visible in the recap command
     */
    @Setter
    private boolean visible;

    public Recap(MCPlayer player) {
        this(player.getInventory(), player.getName(), player.getHealth());
    }

    public Recap(MCPlayerInventory inventory, String playerName, double health) {
        this.playerName = playerName;
        this.startingHealth = health;
        this.lastDamages = Collections.synchronizedList(new ArrayList<>());
        this.visible = false;

        this.inventory = constructInventoryView(inventory);
    }

    private MCInventory constructInventoryView(MCPlayerInventory playerInventory) {
        MCInventory inventory = MCPlatform.getPlatform().createInventory(BattleTracker.getInstance(), 54, playerName + "'s Recap");
        inventory.setContents(playerInventory.getContents().clone());

        inventory.setItem(45, playerInventory.getHelmet().clone());
        inventory.setItem(46, playerInventory.getChestplate().clone());
        inventory.setItem(47, playerInventory.getLeggings().clone());
        inventory.setItem(48, playerInventory.getBoots().clone());

        inventory.setItem(49, playerInventory.getItemInOffHand().clone());
        inventory.setItem(50, playerInventory.getItemInMainHand().clone());

        return inventory;
    }
}
