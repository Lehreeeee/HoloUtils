package me.lehreeeee.HoloUtils.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        DevChatManager devChatManager = DevChatManager.getInstance();
        Player player = event.getPlayer();
        // Ignore if doesnt have perm or admin chat toggled on
        if(!(player.hasPermission("holoutils.devchat") && devChatManager.hasDevChatOn(event.getPlayer().getUniqueId()))) return;

        // Cancel the event
        event.setCancelled(true);

        // Sends the message
        devChatManager.publishMessage(player, MessageUtils.revert(event.message()));
    }

    // This event is called before AsyncChatEvent, need to cancel this cuz i only need AsyncChatEvent
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
