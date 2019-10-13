package org.battleplugins.tracker.tracking.recap;

import mc.alk.mc.MCPlatform;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.inventory.MCInventory;
import mc.alk.mc.inventory.MCPlayerInventory;
import org.battleplugins.tracker.BattleTracker;

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
public class Recap {

    private List<DamageInfo> lastDamages;
    private MCInventory inventory;
    private String playerName;

    private double startingHealth;
    private long deathTime;

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

    /**
     * Returns the player name associated with this recap
     *
     * @return the player name associated with this recap
     */
    public String getPlayerName() {
        return playerName;
    }


    /**
     * Returns the inventory associated with this recap
     *
     * @return the inventory associated with this recap
     */
    public MCInventory getInventory() {
        return inventory;
    }

    /**
     * Returns a list of the last damages dealt

     * @return a list of the last damages dealt
     */
    public List<DamageInfo> getLastDamages() {
        return lastDamages;
    }

    /**
     * Returns the time that the player in this record died
     *
     * @return the time that the player in this record died
     */
    public long getDeathTime() {
        return deathTime;
    }

    /**
     * Returns the amount of health the player originally started with
     *
     * @return the amount of health the player originally started with
     */
    public double getStartingHealth() {
        return startingHealth;
    }

    /**
     * Returns if this recap is visible in the recap command
     *
     * @return if this recap is visible in the recap command
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets if this recap is visible in the recap command
     *
     * @param visible if this recap is visible in the recap command
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.deathTime = System.currentTimeMillis();
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
