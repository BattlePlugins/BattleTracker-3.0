package org.battleplugins.tracker.tracking.recap;

import org.battleplugins.api.entity.living.player.Player;
import org.battleplugins.api.inventory.Inventory;
import org.battleplugins.api.inventory.entity.PlayerInventory;
import org.battleplugins.api.inventory.item.ItemStack;
import org.battleplugins.api.inventory.item.ItemTypes;
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
    private Inventory inventory;

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

    public Recap(Player player) {
        this(player.getInventory(), player.getName(), player.getHealth());
    }

    public Recap(PlayerInventory inventory, String playerName, double health) {
        this.playerName = playerName;
        this.startingHealth = health;
        this.lastDamages = Collections.synchronizedList(new ArrayList<>());
        this.visible = false;

        this.inventory = constructInventoryView(inventory);
    }

    private Inventory constructInventoryView(PlayerInventory playerInventory) {
        Inventory inventory = Inventory.builder().size(54).name(playerName + "'s Recap").build(BattleTracker.getInstance());
        inventory.setContents(playerInventory.getContents().clone());

        inventory.setItem(45, playerInventory.getHelmet().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());
        inventory.setItem(46, playerInventory.getChestplate().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());
        inventory.setItem(47, playerInventory.getLeggings().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());
        inventory.setItem(48, playerInventory.getBoots().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());

        inventory.setItem(49, playerInventory.getItemInOffHand().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());
        inventory.setItem(50, playerInventory.getItemInMainHand().orElse(ItemStack.builder().type(ItemTypes.AIR).build()).clone());

        return inventory;
    }
}
