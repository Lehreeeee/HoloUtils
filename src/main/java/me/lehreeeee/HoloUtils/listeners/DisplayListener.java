package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.GUI.PlayerTagGUIHolder;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.managers.StatusDisplayManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DisplayListener implements Listener {
    private final HoloUtils plugin;
    private final TitleDisplayManager titleDisplayManager = TitleDisplayManager.getInstance();
    private final StatusDisplayManager statusDisplayManager = StatusDisplayManager.getInstance();
    private final NamespacedKey titleNameNSK = new NamespacedKey("holoutils","titlename");

    public DisplayListener(HoloUtils plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInv = event.getClickedInventory();

        // Ignore if its not from my plugin
        if(clickedInv == null || !(clickedInv.getHolder(false) instanceof PlayerTagGUIHolder) || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();
        Entity player = event.getWhoClicked();
        UUID uuid = player.getUniqueId();

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

    // For non-player only
    @EventHandler(ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event){
        statusDisplayManager.updateLocation(event.getEntity().getUniqueId(),event.getTo());
    }

    // For player only
    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        titleDisplayManager.updateLocation(event.getPlayer().getUniqueId(),event.getTo());
    }

    // This is for both LOL
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player)
            titleDisplayManager.removeTitle(entity.getUniqueId());
        else{
            statusDisplayManager.removeStatusDisplay(entity.getUniqueId());
            plugin.checkImmortal(entity);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        titleDisplayManager.removeTitle(uuid);

        // Hehe, idw make another event listener :)
        DevChatManager.getInstance().toggleDevChat(uuid,false);
    }
}
