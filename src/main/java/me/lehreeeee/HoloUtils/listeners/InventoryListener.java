package me.lehreeeee.HoloUtils.listeners;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUIHolder;
import me.lehreeeee.HoloUtils.GUI.RerollGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.RerollManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class InventoryListener implements Listener {
    private final TitleDisplayManager titleDisplayManager = TitleDisplayManager.getInstance();
    private final NamespacedKey titleNameNSK = new NamespacedKey("holoutils","titlename");

    public InventoryListener(HoloUtils plugin){
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInv = event.getClickedInventory();

        if(clickedInv == null) return;

        InventoryHolder invHolder = clickedInv.getHolder(false);

        if (invHolder instanceof PlayerTitleGUIHolder) {
            handlePlayerTitleGUI(event, clickedInv);
            return;
        }

        if (invHolder instanceof RerollGUI) {
            handleRerollGUI(event, clickedInv);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory closedInv = event.getInventory();

        if(!(closedInv.getHolder() instanceof RerollGUI)) return;

        returnItem(closedInv.getItem(11), (Player) event.getPlayer());
    }

    private void handlePlayerTitleGUI(InventoryClickEvent event, Inventory clickedInv){;
        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();
        UUID uuid = event.getWhoClicked().getUniqueId();

        // Remove all player tag
        if(clickedSlot == 49){
            titleDisplayManager.removeTitle(uuid);
            clickedInv.close();
        }
        // Choose tag
        else if (clickedSlot > 8 && clickedSlot < 45 && clickedSlot % 9 != 0 && clickedSlot % 9 != 8) {
            ItemMeta clickedItemMeta = event.getCurrentItem().getItemMeta();
            if(clickedItemMeta == null) return;

            PersistentDataContainer clickedItemPDC = clickedItemMeta.getPersistentDataContainer();
            if(clickedItemPDC.has(titleNameNSK)){
                String titleName = clickedItemPDC.get(titleNameNSK, PersistentDataType.STRING);
                titleDisplayManager.setTitleDisplay(uuid,titleName);
            }
            clickedInv.close();
        }
    }

    private void handleRerollGUI(InventoryClickEvent event, Inventory clickedInv){
        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        // Reroll slot
        if(clickedSlot == 11) {
            ItemStack cursorItem = player.getItemOnCursor();
            ItemStack existingItem = clickedInv.getItem(11);

            // If already has MMOItems in there, return item to their inventory
            returnItem(existingItem,player);

            // If cursor has MMOItems, force move into slot
            if(NBTItem.get(cursorItem).hasType()) {
                clickedInv.setItem(11, cursorItem);
                player.setItemOnCursor(null);

                updateDiceLore(clickedInv,RerollSlotAction.IN, player);
            } else { // Else put back the pane
                ItemStack glassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta glassPaneMeta = glassPane.getItemMeta();
                glassPaneMeta.displayName(MessageHelper.process("<aqua>Put the item you wish to reroll here."));
                glassPane.setItemMeta(glassPaneMeta);

                clickedInv.setItem(11, glassPane);

                updateDiceLore(clickedInv,RerollSlotAction.OUT, null);
            }
            return;
        }

        // Dice button slot
        if(clickedSlot == 15) {
            ItemStack updatedItem = RerollManager.getInstance().reroll(clickedInv.getItem(11), player);

            if(updatedItem != null) {
                player.sendMessage(MessageHelper.process("<aqua>[<gold>Reroll<aqua>] <gold>Item stats have been rerolled! How did it turn out?",false));
                clickedInv.setItem(11, updatedItem);

                // Update requirements again
                updateDiceLore(clickedInv,RerollSlotAction.IN, player);
            }
        }
    }

    private void returnItem(ItemStack item, Player player){
        // Ignore if it's not MMOItem
        if(!NBTItem.get(item).hasType()) return;

        // Attempt to add into their inventory
        HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(item);

        // Drop the item when inventory is full.
        if(!extraItems.isEmpty()){
            player.sendMessage(MessageHelper.process("<aqua>[<gold>Reroll<aqua>] <gold>Looks like your inventory is full, please check the ground for your item!"));
            for (ItemStack extraitem : extraItems.values()){
                if(extraitem != null)
                    player.getWorld().dropItem(player.getLocation(),extraitem);
            }
        }
    }

    private void updateDiceLore(Inventory rerollGUI, RerollSlotAction action, @Nullable Player player){
        ItemStack dice = rerollGUI.getItem(15);

        if (dice == null || !dice.hasItemMeta()) return;

        ItemMeta itemMeta = dice.getItemMeta();

        switch(action){
            case IN -> {
                NBTItem nbtItem = NBTItem.get(rerollGUI.getItem(11));
                String itemKey = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");

                itemMeta.lore(RerollManager.getInstance().getRequirementsLoreList(itemKey, player));
            }
            case OUT -> {
                itemMeta.lore(RerollManager.getInstance().getDefaultDiceLore());
            }
        }

        dice.setItemMeta(itemMeta);
    }
}
