package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.GUI.PlayerTagGUIHolder;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.TagDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class TagDisplayListener implements Listener {
    private final TagDisplayManager tagDisplayManager = TagDisplayManager.getInstance();
    private final NamespacedKey tagNameNSK = new NamespacedKey("holoutils","tagname");

    public TagDisplayListener(HoloUtils plugin){
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInv = event.getClickedInventory();

        // Ignore if its not from my plugin
        if(clickedInv == null || !(clickedInv.getHolder() instanceof PlayerTagGUIHolder) || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();
        Entity player = event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        // Remove all player tag
        if(clickedSlot == 49){
            tagDisplayManager.removeTag(uuid);
            clickedInv.close();
        }
        // Choose tag
        else if (clickedSlot > 8 && clickedSlot < 45 && clickedSlot % 9 != 0 && clickedSlot % 9 != 8) {
            ItemMeta clickedItemMeta = event.getCurrentItem().getItemMeta();
            if(clickedItemMeta == null) return;

            PersistentDataContainer clickedItemPDC = clickedItemMeta.getPersistentDataContainer();
            if(clickedItemPDC.has(tagNameNSK)){
                String tagName = clickedItemPDC.get(tagNameNSK, PersistentDataType.STRING);
                tagDisplayManager.setDisplayTag(uuid,tagName);
            }
            clickedInv.close();
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event){
        tagDisplayManager.updateLocation(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        tagDisplayManager.removeTag(event.getEntity().getUniqueId());
    }
}
