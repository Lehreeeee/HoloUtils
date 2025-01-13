package me.lehreeeee.HoloUtils.listeners;

import me.lehreeeee.HoloUtils.HoloUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.logging.Logger;

public class EntityDamageListener implements Listener {

    private final HoloUtils plugin;
    private final Logger logger;

    public EntityDamageListener(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMobAttack(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        debugLogger(damager.getName());
    }

    public void debugLogger(String debugMessage){
        if(plugin.getConfig().getBoolean("debug",false))
            logger.info(debugMessage);
    }

}
