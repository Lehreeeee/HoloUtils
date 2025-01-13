package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;
import java.util.logging.Logger;

public class TextDisplayManager {
    private final HoloUtils plugin;
    private final Logger logger;

    public TextDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void addDisplay(UUID uuid){

        Entity targetEntity = Bukkit.getEntity(uuid);

        if (targetEntity == null) {
            logger.warning("Cant find the entity.");
            return;
        }

        World world = targetEntity.getWorld();
        Location location = targetEntity.getLocation();

        TextDisplay display = world.spawn(location, TextDisplay.class, entity -> {
            entity.setPersistent(true);
        });

        logger.info("Adding display text");
        display.text(MessageHelper.process("<aqua>[<#FFA500>TEST DISPLAY TAG HERE<aqua>]"));

        logger.info("Adding display to entity passenger");
        targetEntity.addPassenger(display);

        logger.info("Scheduling to remove display after 5s");
        Bukkit.getScheduler().runTaskLater(plugin, display::remove, 100L);
    }
}
