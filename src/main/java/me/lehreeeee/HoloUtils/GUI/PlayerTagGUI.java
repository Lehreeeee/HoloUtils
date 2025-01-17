package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.TagDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class PlayerTagGUI {


    public Inventory createPlayerTagGUI(Player player){
        TagDisplayManager tagDisplayManager = TagDisplayManager.getInstance();
        Inventory inv = new PlayerTagGUIHolder().getInventory();


        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageHelper.process("<#FFA500><!i>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Remove tag button
        ItemStack removeButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta updateButtonMeta = removeButton.getItemMeta();
        if (updateButtonMeta != null){
            updateButtonMeta.displayName(MessageHelper.process("<red><b><!i>Remove player tag"));
            removeButton.setItemMeta(updateButtonMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (isBorderSlot(i)) {
                inv.setItem(i, fillGlassPane);
            }
        }

        Map<String,String> availableTags = tagDisplayManager.getAvailableTags(player);
        int tagSlot = 9;

        for(String tagName : availableTags.keySet()){
            while(isBorderSlot(tagSlot)){
                tagSlot++;
            }

            // TODO: Add more pages for more than 28 Tags
            // Make sure its within the inventory, 43 is the last available slot
            if(tagSlot < 44){
                // Populate the inventory with available tags, how the tags look will be shown as the item name.
                ItemStack tagItem = createTagItem(availableTags.get(tagName));
                inv.setItem(tagSlot,tagItem);
                tagSlot++;
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

    private ItemStack createTagItem(String tag){
        ItemStack tagItem = new ItemStack(Material.NAME_TAG);
        ItemMeta tagItemMeta = tagItem.getItemMeta();

        if(tagItemMeta != null){
            // Need add <!i> or its gonna be italic for some reason
            tagItemMeta.displayName(MessageHelper.process("<!i>" + tag));
            tagItem.setItemMeta(tagItemMeta);
        }

        return tagItem;
    }
}
