package me.lehreeeee.HoloUtils.listeners;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.GUI.EventRewardsGUI;
import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUIHolder;
import me.lehreeeee.HoloUtils.GUI.RerollGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.EventRewardsManager;
import me.lehreeeee.HoloUtils.managers.RerollManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import me.lehreeeee.HoloUtils.utils.SoundUtils;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
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
    private final NamespacedKey rewardIdNSK = new NamespacedKey("holoutils","reward_id");
    private final NamespacedKey rowIdNSK = new NamespacedKey("holoutils","row_id");
    private final NamespacedKey pageNSK = new NamespacedKey("holoutils","page");

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
            return;
        }

        if (invHolder instanceof EventRewardsGUI) {
            handleEventRewardsGUI(event, clickedInv);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryShiftClick(InventoryClickEvent event){

        // Ignore non shift click
        if(!event.getClick().isShiftClick()) return;

        // Ignore non reroll gui
        Inventory topInv = event.getWhoClicked().getOpenInventory().getTopInventory();
        if(!(topInv.getHolder() instanceof RerollGUI)) return;

        // Let other listener handle if clicked inv is reroll gui
        Inventory clickedInv = event.getClickedInventory();
        if(clickedInv == null || clickedInv.getHolder() instanceof RerollGUI) return;

        ItemStack clickedItem = event.getCurrentItem();

        // If clicked item is MMOItems, cancel event and force move into slot
        if(NBTItem.get(clickedItem).hasType()) {
            event.setCancelled(true);
            // If already has MMOItems in there, return item to their inventory
            Player player = (Player) event.getWhoClicked();
            returnItem(topInv.getItem(11),player);

            topInv.setItem(11, clickedItem);
            event.setCurrentItem(null);

            updateDiceLore(topInv,RerollSlotAction.IN, player);
            updateTemplateItem(topInv,RerollSlotAction.IN);
            SoundUtils.playSound(player,"block.amethyst_block.place");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory closedInv = event.getInventory();
        InventoryHolder invHolder = closedInv.getHolder(false);

        if(invHolder instanceof RerollGUI){
            returnItem(closedInv.getItem(11), (Player) event.getPlayer());
            return;
        }

        if (invHolder instanceof EventRewardsGUI) {
            EventRewardsManager.getInstance().clearPlayerRewardsCache(String.valueOf(event.getPlayer().getUniqueId()));
        }
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
            ItemStack item = event.getCurrentItem();
            if(item == null || item.getType() == Material.AIR) return;

            ItemMeta clickedItemMeta = item.getItemMeta();
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
                updateTemplateItem(clickedInv,RerollSlotAction.IN);
                SoundUtils.playSound(player,"block.amethyst_block.place");
            } else { // Else put back the pane
                ItemStack glassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta glassPaneMeta = glassPane.getItemMeta();
                glassPaneMeta.displayName(MessageHelper.process("<aqua>Put the item you wish to reroll here."));
                glassPane.setItemMeta(glassPaneMeta);

                clickedInv.setItem(11, glassPane);

                updateDiceLore(clickedInv,RerollSlotAction.OUT, null);
                updateTemplateItem(clickedInv,RerollSlotAction.OUT);
            }
            return;
        }

        // Dice button slot
        if(clickedSlot == 15) {
            ItemStack updatedItem = RerollManager.getInstance().reroll(clickedInv.getItem(11), player);

            if(updatedItem != null) {
                SoundUtils.playSound(player,"block.amethyst_block.resonate");
                player.sendMessage(MessageHelper.process("<aqua>[<gold>Reroll<aqua>] <gold>Item stats have been rerolled!",false));
                clickedInv.setItem(11, updatedItem);

                // Update requirements again
                updateDiceLore(clickedInv,RerollSlotAction.IN, player);
            } else {
                SoundUtils.playSound(player,"entity.villager.no");
            }
        }
    }

    private void handleEventRewardsGUI(InventoryClickEvent event, Inventory clickedInv){
        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        // Claim all button
        if(clickedSlot == 49){
            clickedInv.close();
            EventRewardsManager.getInstance().claimAllRewards(player);
        }
        // Previous Page
        else if (clickedSlot == 48){
            PersistentDataContainer claimAllButtonPDC = clickedInv.getItem(49).getItemMeta().getPersistentDataContainer();

            if(claimAllButtonPDC.has(pageNSK)){
                int newPage = claimAllButtonPDC.get(pageNSK, PersistentDataType.INTEGER) - 1;
                if(newPage < 1) return;
                EventRewardsManager.getInstance().updateInventory(String.valueOf(player.getUniqueId()),newPage,clickedInv);
                SoundUtils.playSound(player,"item.book.page_turn");
            }
        }
        // Next Page
        else if (clickedSlot == 50){
            PersistentDataContainer claimAllButtonPDC = clickedInv.getItem(49).getItemMeta().getPersistentDataContainer();

            if(claimAllButtonPDC.has(pageNSK)){
                int newPage = claimAllButtonPDC.get(pageNSK, PersistentDataType.INTEGER) + 1;
                EventRewardsManager.getInstance().updateInventory(String.valueOf(player.getUniqueId()),newPage,clickedInv);
                SoundUtils.playSound(player,"item.book.page_turn");
            }
        }
        // Claim specific reward
        else if (clickedSlot > 8 && clickedSlot < 45 && clickedSlot % 9 != 0 && clickedSlot % 9 != 8) {
            ItemStack item = event.getCurrentItem();
            if(item == null || item.getType().isAir()) return;
            ItemMeta clickedItemMeta = item.getItemMeta();
            if(clickedItemMeta == null) return;

            PersistentDataContainer clickedItemPDC = clickedItemMeta.getPersistentDataContainer();
            if(clickedItemPDC.has(rewardIdNSK) && clickedItemPDC.has(rowIdNSK)) {
                String rowId = clickedItemPDC.get(rowIdNSK, PersistentDataType.STRING);
                String rewardId = clickedItemPDC.get(rewardIdNSK, PersistentDataType.STRING);

                // Ignore invalid reward
                if(!EventRewardsManager.getInstance().isRewardIdValid(rewardId)) {
                    SoundUtils.playSound(player,"block.chest.locked");
                    player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] This reward is not set up correctly, please report to a developer.",false));
                    return;
                }

                // Remove before giving rewards to prevent double claiming
                clickedInv.setItem(clickedSlot,null);
                EventRewardsManager.getInstance().claimReward(player, clickedInv, rowId);
            }
        }
    }

    private void returnItem(ItemStack item, Player player){
        // Ignore if it's not MMOItem
        if(!NBTItem.get(item).hasType()) return;

        // Attempt to add into their inventory
        HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(item);
        SoundUtils.playSound(player,"block.amethyst_block.place");

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

    private void updateTemplateItem(Inventory rerollGUI, RerollSlotAction action){
        switch(action){
            case IN -> {
                NBTItem nbtItem = NBTItem.get(rerollGUI.getItem(11));
                String itemKey = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");

                if(!RerollManager.getInstance().isRerollable(itemKey)) {
                    updateTemplateItem(rerollGUI,RerollSlotAction.OUT);
                    return;
                }

                MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(nbtItem);

                if(template != null){
                    rerollGUI.setItem(13,new ConfigMMOItem(template,1).getPreview());
                }
            }
            case OUT -> {
                ItemStack templatePane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta templatePaneMeta = templatePane.getItemMeta();
                templatePaneMeta.displayName(MessageHelper.process("<aqua>Item template will be shown here."));

                templatePane.setItemMeta(templatePaneMeta);

                rerollGUI.setItem(13,templatePane);
            }
        }
    }
}
