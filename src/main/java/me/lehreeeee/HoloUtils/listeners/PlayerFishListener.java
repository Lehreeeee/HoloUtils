package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.AutoCaptchaManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.logging.Logger;

public class PlayerFishListener implements Listener {

    private final Logger logger;

    public PlayerFishListener(HoloUtils plugin){
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event){
        Player player = event.getPlayer();
        AutoCaptchaManager manager = AutoCaptchaManager.getInstance();
        boolean captchaTriggered =  manager.executeFishCheck(player.getName());
        if(captchaTriggered){
            logger.info("[HoloUtils] " + player.getName() + " has triggered the AutoCaptcha!");
        }
    }

}
