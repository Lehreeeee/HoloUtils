package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.logging.Logger;

public class TextDisplayManager {
    private final HoloUtils plugin;
    private final Logger logger;

    public TextDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void addDisplay(UUID uuid, String tag){

        Entity targetEntity = Bukkit.getEntity(uuid);

        if (targetEntity == null) {
            logger.warning("Cant find the entity.");
            return;
        }

        World world = targetEntity.getWorld();
        Location location = targetEntity.getLocation();

        TextDisplay display = world.spawn(location, TextDisplay.class, entity -> {
            entity.setPersistent(true);
            entity.setBillboard(Display.Billboard.CENTER);

            entity.setVisibleByDefault(false);
            entity.setTransformation(
                    new Transformation(
                            new Vector3f(0, 0.6F, 0),
                            new AxisAngle4f(), // no left rotation
                            new Vector3f(1,1,1), // scale up by a factor of 2 on all axes
                            new AxisAngle4f() // no right rotation
                    )
            );
        });

        logger.info("Adding display text");
        display.text(MessageHelper.process(tag));

        logger.info("Adding display to entity passenger");
        targetEntity.addPassenger(display);
        display.setVisibleByDefault(true);

        logger.info("Scheduling to remove display after 5s");
        Bukkit.getScheduler().runTaskLater(plugin, display::remove, 100L);
    }
}
