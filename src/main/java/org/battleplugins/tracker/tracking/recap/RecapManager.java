package org.battleplugins.tracker.tracking.recap;

import mc.alk.battlecore.util.TimeUtil;
import mc.alk.mc.ChatColor;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.inventory.ItemBuilder;
import mc.alk.mc.inventory.MCInventory;
import mc.alk.mc.inventory.MCItemStack;
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
import java.util.Optional;
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
    public void sendArmorRecap(MCPlayer player, Recap recap) {
        MCItemStack empty = ItemBuilder.builder().setType("bone").build();
        MCInventory recapInventory = recap.getInventory();
        MCInventory inventory = plugin.getPlatform().createInventory(plugin, 54, recap.getPlayerName() + "'s Recap");
        inventory.setItem(13, Optional.of(recapInventory.getItem(45).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));
        inventory.setItem(22, Optional.of(recapInventory.getItem(46).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));
        inventory.setItem(31, Optional.of(recapInventory.getItem(47).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));
        inventory.setItem(40, Optional.of(recapInventory.getItem(48).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));

        inventory.setItem(21, Optional.of(recapInventory.getItem(49).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));
        inventory.setItem(23, Optional.of(recapInventory.getItem(50).clone()).filter(stack -> !stack.getType().equalsIgnoreCase("air")).orElse(empty));

        inventory.setItem(25, getRecapBook(recap));
        player.openInventory(inventory);
    }

    /**
     * Sends the inventory recap click event to the specified player
     *
     * @param player the player to send the click even to
     * @param recap the recap of the player who died
     */
    public void sendInventoryRecap(MCPlayer player, Recap recap) {
        MCInventory inventory = recap.getInventory();
        inventory.setItem(52, getRecapBook(recap));

        player.openInventory(inventory);
    }

    // TODO: Add message translations for this
    private MCItemStack getRecapBook(Recap recap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        List<String> lore = new ArrayList<>(Arrays.asList(ChatColor.GRAY + "Death Time: " + ChatColor.YELLOW + TimeUtil.convertToShortString(System.currentTimeMillis() - recap.getDeathTime()) + "ago",
                ChatColor.GRAY + "Starting Health: " + ChatColor.GREEN + "♥ +" + decimalFormat.format(recap.getStartingHealth() / 2),
                "",
                ChatColor.GOLD + "Damage Log:"));

        // Obtains last 10 damages dealt
        List<DamageInfo> damageInfos = recap.getLastDamages().stream().sorted((recap1, recap2) -> (int) (recap2.getLogTime() - recap1.getLogTime())).collect(Collectors.toList());
        int max = damageInfos.size();
        if (max > 10)
            max = 10;

        for (int i = damageInfos.size() - max; i < damageInfos.size(); i++) {
            DamageInfo damageInfo = damageInfos.get(i);
            String timeAgo = "(" + TimeUtil.convertToShortString(System.currentTimeMillis() - damageInfo.getLogTime()).trim() + ")";

            lore.add(ChatColor.RED + "♥ -" + decimalFormat.format(damageInfo.getDamage() / 2) + " " + ChatColor.YELLOW + timeAgo + " " + ChatColor.AQUA + TrackerUtil.capitalizeFirst(damageInfo.getCause().replace("_", " ")));
        }

        return ItemBuilder.builder()
                .setType("book")
                .setQuantity(1)
                .setDisplayName(ChatColor.GOLD + "Recap Information:")
                .setLore(lore)
                .build();
    }
}
