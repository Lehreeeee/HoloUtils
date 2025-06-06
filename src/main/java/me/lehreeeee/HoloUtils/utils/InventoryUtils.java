package me.lehreeeee.HoloUtils.utils;

import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryUtils {
    public static boolean isBorderSlot(int slot){
        return slot <= 8 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
    }

    public static void giveItem(List<ItemStack> items, Player player) {
        giveItem(items, player, false);
    }

    public static void giveItem(List<ItemStack> items, Player player, boolean onlyMMOItem) {
        List<ItemStack> leftoverItems = new ArrayList<>();

        int amountGiven = 0;

        for (ItemStack item : items) {
            if (item == null) continue;

            if (onlyMMOItem && !NBTItem.get(item).hasType()) continue;

            amountGiven++;

            HashMap<Integer, ItemStack> extras = player.getInventory().addItem(item);
            leftoverItems.addAll(extras.values());
        }

        if(amountGiven > 0) SoundUtils.playSound(player, "block.amethyst_block.place");

        if (!leftoverItems.isEmpty()) {
            player.sendMessage(MessageUtils.process("<gold>Looks like your inventory is full, please check the ground for your item(s)!",true));

            leftoverItems.forEach((item) -> player.getWorld().dropItem(player.getLocation(),item));

            LoggerUtils.info(player.getName()
                    + "'s inventory was full when attempting to give items. Dropping "
                    + leftoverItems.size()
                    + " items at: "
                    + player.getLocation());
        }
    }
}
