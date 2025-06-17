package me.lehreeeee.HoloUtils.listeners;


import me.lehreeeee.HoloUtils.managers.DamageLeaderboardManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class EntityDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        UUID victimUUID = victim.getUniqueId();

        if(!DamageLeaderboardManager.getInstance().isEntityTrackedOrLinked(victimUUID)){
            return;
        }

        // Handle projectile
        if(damager instanceof Projectile proj) {
            // Set damager to arrow shooter instead of arrow
            if(proj.getShooter() instanceof Entity shooter) {
                damager = shooter;
            }
        }

        if(!(damager instanceof Player) || !(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        double finalDamage = event.getFinalDamage();

        // No dmg overflow
        finalDamage = Math.min(finalDamage, livingVictim.getHealth());

        if(finalDamage > 0) {
            DamageLeaderboardManager.getInstance().addDamage(victimUUID,damager.getUniqueId(),finalDamage);
        }
    }
}
