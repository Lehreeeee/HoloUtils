package me.lehreeeee.HoloUtils.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Base64;
import java.util.UUID;

public class ItemUtils {
    public static String encodeItem(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decodeItem(String base64) {
        return org.bukkit.inventory.ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }

    public static ItemStack getGUIArrowButton(boolean isLeft){
        ItemStack arrowButton = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) arrowButton.getItemMeta();

        String displayName = isLeft ? "<red>Previous Page" : "<red>Next Page";
        String skullTexture = isLeft
                ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzJmZjhhYWE0YjJlYzMwYmM1NTQxZDQxYzg3ODIxOTliYWEyNWFlNmQ4NTRjZGE2NTFmMTU5OWU2NTRjZmM3OSJ9fX0="
                : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";

        if(skullMeta != null){
            skullMeta.displayName(MessageUtils.process(displayName));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add((new ProfileProperty("textures", skullTexture)));
            skullMeta.setPlayerProfile(profile);

            arrowButton.setItemMeta(skullMeta);
        }

        return arrowButton;
    }
}
