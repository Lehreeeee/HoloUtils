package me.lehreeeee.HoloUtils.utils;

public class InventoryUtils {
    public static boolean isBorderSlot(int slot){
        return slot <= 8 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
    }
}
