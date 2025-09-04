package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.AutoCaptchaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.logging.Logger;

public class PlayerFishListener implements Listener {

    private final Logger logger;

    public PlayerFishListener(HoloUtils plugin){
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event){
        if(event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        Player player = event.getPlayer();
        AutoCaptchaManager manager = AutoCaptchaManager.getInstance();
        manager.executeFishCheck(player.getName());
    }

}
