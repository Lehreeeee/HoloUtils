package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.ItemUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public class PlayerTitleGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    public PlayerTitleGUI(Player player){
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.process(TitleDisplayManager.getInstance().getGUIName()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public Inventory getPlayerTitleGUI(){
        TitleDisplayManager titleDisplayManager = TitleDisplayManager.getInstance();

        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageUtils.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Remove tag button
        ItemStack removeTitleButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta removeTitleButtonItemMeta = removeTitleButton.getItemMeta();
        if (removeTitleButtonItemMeta != null){
            removeTitleButtonItemMeta.displayName(MessageUtils.process("<red><b>Remove player title"));

            PersistentDataContainer removeTitleButtonPDC = removeTitleButtonItemMeta.getPersistentDataContainer();
            removeTitleButtonPDC.set(new NamespacedKey("holoutils","page"), PersistentDataType.INTEGER, 1);

            removeTitleButton.setItemMeta(removeTitleButtonItemMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (InventoryUtils.isBorderSlot(i)) {
                inventory.setItem(i, fillGlassPane);
            }
        }

        populateInventory(player,1);

        // Set remove and arrows button
        inventory.setItem(49, removeTitleButton);
        inventory.setItem(48, ItemUtils.getGUIArrowButton(true));
        inventory.setItem(50, ItemUtils.getGUIArrowButton(false));

        return inventory;
    }

    public void populateInventory(Player player, int page){
        Map<String,String> availableTags = TitleDisplayManager.getInstance().getAvailableTitles(player);

        // Skip all previous titles
        int toRemove = (page - 1) * 28;
        Iterator<Map.Entry<String, String>> iterator = availableTags.entrySet().iterator();

        while (iterator.hasNext() && toRemove-- > 0) {
            iterator.next();
            iterator.remove();
        }

        if(availableTags.isEmpty()) return;

        // Update Page Number
        ItemStack removeTitleButton = inventory.getItem(49);
        ItemMeta removeTitleButtonMeta = removeTitleButton.getItemMeta();
        removeTitleButtonMeta.getPersistentDataContainer().set(new NamespacedKey("holoutils","page"), PersistentDataType.INTEGER, page);
        removeTitleButton.setItemMeta(removeTitleButtonMeta);

        // Clear the page
        for(int i = 10; i <= 43; i++){
            if(i % 9 == 0 || i % 9 == 8) continue;

            inventory.setItem(i, null);
        }

        int titleSlot = 10;

        for(String tagName : availableTags.keySet()){
            while(titleSlot < 44 && InventoryUtils.isBorderSlot(titleSlot)){
                titleSlot++;
            }
            // Make sure its within the inventory, 43 is the last available slot
            if(titleSlot < 44){
                // Populate the inventory with available titles, how the titles look will be shown as the item name.
                ItemStack tagItem = createTitleItem(tagName, availableTags.get(tagName));
                inventory.setItem(titleSlot,tagItem);
                titleSlot++;
            }
        }
    }

    private ItemStack createTitleItem(String titleName, String title){
        ItemStack titleItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleItemMeta = titleItem.getItemMeta();

        if(titleItemMeta != null){
            // Need add <!i> or its gonna be italic for some reason
            titleItemMeta.displayName(MessageUtils.process(title));

            PersistentDataContainer titleItemPDC = titleItemMeta.getPersistentDataContainer();
            titleItemPDC.set(new NamespacedKey("holoutils","titlename"), PersistentDataType.STRING, titleName);

            titleItem.setItemMeta(titleItemMeta);
        }

        return titleItem;
    }
}
