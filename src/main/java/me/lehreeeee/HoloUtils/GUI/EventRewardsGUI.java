package me.lehreeeee.HoloUtils.GUI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.managers.EventRewardsManager;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EventRewardsGUI implements InventoryHolder {
    private final Inventory inventory;

    public EventRewardsGUI(){
        this.inventory = Bukkit.createInventory(this, 54, MessageHelper.process("<gold>Event Rewards"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public Inventory getEventRewardsInventory(String uuid){
        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageHelper.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Claim all button
        ItemStack claimAllButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta claimAllButtonMeta = claimAllButton.getItemMeta();
        if (claimAllButtonMeta != null){
            claimAllButtonMeta.displayName(MessageHelper.process("<green><b>Claim All"));

            PersistentDataContainer updateButtonPDC = claimAllButtonMeta.getPersistentDataContainer();
            updateButtonPDC.set(new NamespacedKey("holoutils","page"), PersistentDataType.INTEGER, 1);

            claimAllButton.setItemMeta(claimAllButtonMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (InventoryUtils.isBorderSlot(i)) {
                inventory.setItem(i, fillGlassPane);
            }
        }

        // Set claim all button and arrows
        inventory.setItem(49, claimAllButton);
        inventory.setItem(48, createArrowButton(true));
        inventory.setItem(50, createArrowButton(false));

        // Insert rewards after data returned via callback
        EventRewardsManager.getInstance().getAllRewards(uuid, inventory);

        return inventory;
    }

    private ItemStack createArrowButton(boolean isLeft){
        ItemStack arrowButton = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) arrowButton.getItemMeta();

        String displayName = isLeft ? "<red>Previous Page" : "<red>Next Page";
        String skullTexture = isLeft
                ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzJmZjhhYWE0YjJlYzMwYmM1NTQxZDQxYzg3ODIxOTliYWEyNWFlNmQ4NTRjZGE2NTFmMTU5OWU2NTRjZmM3OSJ9fX0="
                : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";

        if(skullMeta != null){
            skullMeta.displayName(MessageHelper.process(displayName));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add((new ProfileProperty("textures", skullTexture)));
            skullMeta.setPlayerProfile(profile);

            arrowButton.setItemMeta(skullMeta);
        }

        return arrowButton;
    }
}
