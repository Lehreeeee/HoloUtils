package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class PlayerTitleGUI {


    public Inventory createPlayerTitleGUI(Player player){
        TitleDisplayManager titleDisplayManager = TitleDisplayManager.getInstance();
        Inventory inv = new PlayerTitleGUIHolder().getInventory();


        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageHelper.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Remove tag button
        ItemStack removeButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta updateButtonMeta = removeButton.getItemMeta();
        if (updateButtonMeta != null){
            updateButtonMeta.displayName(MessageHelper.process("<red><b>Remove player title"));
            removeButton.setItemMeta(updateButtonMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (isBorderSlot(i)) {
                inv.setItem(i, fillGlassPane);
            }
        }

        Map<String,String> availableTags = titleDisplayManager.getAvailableTitles(player);
        int titleSlot = 9;

        for(String tagName : availableTags.keySet()){
            while(isBorderSlot(titleSlot)){
                titleSlot++;
            }

            // TODO: Add more pages for more than 28 Tags
            // Make sure its within the inventory, 43 is the last available slot
            if(titleSlot < 44){
                // Populate the inventory with available titles, how the titles look will be shown as the item name.
                ItemStack tagItem = createTitleItem(tagName, availableTags.get(tagName));
                inv.setItem(titleSlot,tagItem);
                titleSlot++;
            }
        }

        // Set remove button
        inv.setItem(49, removeButton);

        return inv;
    }

    // Helper method to check if a slot is part of the border
    private boolean isBorderSlot(int slot){
        return slot <= 8 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
    }

    private ItemStack createTitleItem(String titleName, String title){
        ItemStack titleItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleItemMeta = titleItem.getItemMeta();

        if(titleItemMeta != null){
            // Need add <!i> or its gonna be italic for some reason
            titleItemMeta.displayName(MessageHelper.process(title));

            PersistentDataContainer titleItemPDC = titleItemMeta.getPersistentDataContainer();
            titleItemPDC.set(new NamespacedKey("holoutils","titlename"), PersistentDataType.STRING, titleName);

            titleItem.setItemMeta(titleItemMeta);
        }

        return titleItem;
    }
}
