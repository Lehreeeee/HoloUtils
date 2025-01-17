package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.GUI.PlayerTagGUIHolder;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.TagDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class InventoryInteractionListener implements Listener {
    private final TagDisplayManager tagDisplayManager;

    public InventoryInteractionListener(HoloUtils plugin){
        Bukkit.getPluginManager().registerEvents(this,plugin);
        this.tagDisplayManager = TagDisplayManager.getInstance();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInv = event.getClickedInventory();

        if(clickedInv == null || !(clickedInv.getHolder() instanceof PlayerTagGUIHolder)) return;

        event.setCancelled(true);
        int clickedSlot = event.getRawSlot();
        Entity player = event.getWhoClicked();
        //UUID uuid = player.getUniqueId();

        // Remove all player tag
        if(clickedSlot == 49){

        }
        // Choose tag
        else if (clickedSlot > 8 && clickedSlot < 45 && clickedSlot % 9 != 0 && clickedSlot % 9 != 8) {

        }
    }
}
