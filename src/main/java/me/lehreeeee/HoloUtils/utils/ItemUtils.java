package me.lehreeeee.HoloUtils.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemUtils {
    public static String encodeItem(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decodeItem(String base64) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }
}
