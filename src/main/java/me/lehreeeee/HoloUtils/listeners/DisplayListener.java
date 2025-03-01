package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.managers.StatusDisplayManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class DisplayListener implements Listener {
    private final HoloUtils plugin;
    private final TitleDisplayManager titleDisplayManager = TitleDisplayManager.getInstance();
    private final StatusDisplayManager statusDisplayManager = StatusDisplayManager.getInstance();

    public DisplayListener(HoloUtils plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
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
