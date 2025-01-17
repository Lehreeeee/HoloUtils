package me.lehreeeee.HoloUtils.listeners;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.lehreeeee.HoloUtils.HoloUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PlayerProjectileListener implements Listener {

    private final HoloUtils plugin;
    private final Logger logger;
    private Set<String> disabledWorlds = new HashSet<>();

    public PlayerProjectileListener(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerArrowHit(ProjectileHitEvent event) {
        // Not an arrow? Ignore
        if(!(event.getEntity() instanceof Arrow arrow)) return;
        // Not from player? Ignore too
        if(!(arrow.getShooter() instanceof Player shooter)) return;
        // Hit non player? Ignore too
        if(!(event.getHitEntity() instanceof Player)) return;

        // Cancel it if arrow hits on players is disabled in this world
        if(disabledWorlds.contains(shooter.getWorld().getName())){
            // Passing thru, woohoo
            debugLogger("Cancelling ProjectileHitEvent");
            event.setCancelled(true);
        }
    }

    public void setDisabledWorlds(Set<String> disabledWorlds){
        this.disabledWorlds = disabledWorlds;
    }

    public void debugLogger(String debugMessage){
        if(plugin.getConfig().getBoolean("debug",false))
            logger.info(debugMessage);
    }

}
