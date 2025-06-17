package me.lehreeeee.HoloUtils.listeners;

import com.Zrips.CMI.events.CMIPlayerUnVanishEvent;
import com.Zrips.CMI.events.CMIPlayerVanishEvent;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CMIListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerVanish(CMIPlayerVanishEvent event){
        TitleDisplayManager.getInstance().toggleTitle(event.getPlayer().getUniqueId(),false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerUnVanish(CMIPlayerUnVanishEvent event){
        TitleDisplayManager.getInstance().toggleTitle(event.getPlayer().getUniqueId(),
                event.getPlayer().getGameMode() != GameMode.SPECTATOR);
    }
}
