package org.battleplugins.tracker.tracking.recap;

import mc.alk.battlecore.util.TimeUtil;

import org.battleplugins.api.entity.living.player.Player;
import org.battleplugins.api.inventory.Inventory;
import org.battleplugins.api.inventory.item.ItemStack;
import org.battleplugins.api.inventory.item.ItemTypes;
import org.battleplugins.api.inventory.item.component.DisplayNameComponent;
import org.battleplugins.api.inventory.item.component.LoreComponent;
import org.battleplugins.api.message.MessageStyle;
import org.battleplugins.tracker.BattleTracker;
import org.battleplugins.tracker.util.TrackerUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Main recap manager for BattleTracker.
 *
 * @author Redned
 */
@RequiredArgsConstructor
public class RecapManager {

    @NonNull
    private BattleTracker plugin;

    /**
     * A map of all the "dead" recaps.
     *
     * String: the name of the player
     * Value: the remap value
     *
     * @return a map of all the current recaps
     */
    @Getter
    private Map<String, Recap> deathRecaps = Collections.synchronizedMap(new ConcurrentHashMap<>());

    /**
     * Sends the armor recap click event to the specified player
     *
     * @param player the player to send the click even to
     * @param recap the recap of the player who died
     */
    public void sendArmorRecap(Player player, Recap recap) {
        ItemStack empty = ItemStack.builder().type(ItemTypes.BONE).component(DisplayNameComponent.class, "").build();
        Inventory recapInventory = recap.getInventory();
        Inventory inventory = Inventory.builder().size(54).name(recap.getPlayerName() + "'s Recap").build(plugin);
        inventory.setItem(13, recapInventory.getItem(45).orElse(empty).clone());
        inventory.setItem(22, recapInventory.getItem(46).orElse(empty).clone());
        inventory.setItem(31, recapInventory.getItem(47).orElse(empty).clone());
        inventory.setItem(40, recapInventory.getItem(48).orElse(empty).clone());

        inventory.setItem(21, recapInventory.getItem(49).orElse(empty).clone());
        inventory.setItem(23, recapInventory.getItem(50).orElse(empty).clone());

        inventory.setItem(25, getRecapBook(recap));
        player.openInventory(inventory);
    }

    /**
     * Sends the inventory recap click event to the specified player
     *
     * @param player the player to send the click even to
     * @param recap the recap of the player who died
     */
    public void sendInventoryRecap(Player player, Recap recap) {
        Inventory inventory = recap.getInventory();
        inventory.setItem(52, getRecapBook(recap));

        player.openInventory(inventory);
    }

    // TODO: Add message translations for this
    private ItemStack getRecapBook(Recap recap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        List<String> lore = new ArrayList<>(Arrays.asList(MessageStyle.GRAY + "Death Time: " + MessageStyle.YELLOW + TimeUtil.convertToShortString(System.currentTimeMillis() - recap.getDeathTime()) + "ago",
                MessageStyle.GRAY + "Starting Health: " + MessageStyle.GREEN + "♥ +" + decimalFormat.format(recap.getStartingHealth() / 2),
                "",
                MessageStyle.GOLD + "Damage Log:"));

        // Obtains last 10 damages dealt
        List<DamageInfo> damageInfos = recap.getLastDamages().stream().sorted((recap1, recap2) -> (int) (recap2.getLogTime() - recap1.getLogTime())).collect(Collectors.toList());
        int max = damageInfos.size();
        if (max > 10)
            max = 10;

        for (int i = damageInfos.size() - max; i < damageInfos.size(); i++) {
            DamageInfo damageInfo = damageInfos.get(i);
            String timeAgo = "(" + TimeUtil.convertToShortString(System.currentTimeMillis() - damageInfo.getLogTime()).trim() + ")";

            lore.add(MessageStyle.RED + "♥ -" + decimalFormat.format(damageInfo.getDamage() / 2) + " " + MessageStyle.YELLOW + timeAgo + " " + MessageStyle.AQUA + TrackerUtil.capitalizeFirst(damageInfo.getCause().replace("_", " ")));
        }

        return ItemStack.builder()
                .type(ItemTypes.BOOK)
                .quantity(1)
                .component(DisplayNameComponent.class, MessageStyle.GOLD + "Recap Information:")
                .component(LoreComponent.class, lore)
                .build();
    }
}
