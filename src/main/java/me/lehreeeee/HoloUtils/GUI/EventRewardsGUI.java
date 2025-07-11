package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.EventRewardsManager;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.ItemUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class EventRewardsGUI implements InventoryHolder {
    private final Inventory inventory;

    public EventRewardsGUI(){
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.process("<gold>Event Rewards"));
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
            fillGlassPaneMeta.displayName(MessageUtils.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Claim all button
        ItemStack claimAllButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta claimAllButtonMeta = claimAllButton.getItemMeta();
        if (claimAllButtonMeta != null){
            claimAllButtonMeta.displayName(MessageUtils.process("<green><b>Claim All"));

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
        inventory.setItem(48, ItemUtils.getGUIArrowButton(true));
        inventory.setItem(50, ItemUtils.getGUIArrowButton(false));

        // Insert rewards after data returned via callback
        EventRewardsManager.getInstance().getAllRewards(uuid, inventory);

        return inventory;
    }
}
