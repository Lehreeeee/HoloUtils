package me.lehreeeee.HoloUtils.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    public PlayerChatListener(HoloUtils plugin){
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        DevChatManager devChatManager = DevChatManager.getInstance();
        Player player = event.getPlayer();
        // Ignore if doesnt have perm or admin chat toggled on
        if(!(player.hasPermission("holoutils.devchat") && devChatManager.hasDevChatOn(event.getPlayer().getUniqueId()))) return;

        // Cancel the event
        event.setCancelled(true);

        // Sends the message
        devChatManager.publishMessage(player, MessageHelper.revert(event.message()));
    }

    // Legacy support
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){
        DevChatManager devChatManager = DevChatManager.getInstance();
        Player player = event.getPlayer();
        // Ignore if doesnt have perm or admin chat toggled on
        if(!(player.hasPermission("holoutils.devchat") && devChatManager.hasDevChatOn(event.getPlayer().getUniqueId()))) return;

        // Cancel the event
        event.setCancelled(true);
    }
}
