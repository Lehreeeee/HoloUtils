package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.logging.Logger;

public class TagDisplayManager {
    private static TagDisplayManager instance;
    private final HoloUtils plugin;
    private final Logger logger;
    private final Map<String,String> playerTags = new HashMap<>();
    private String GUITitle = "<white>[<aqua>Display Tag<white>]";

    private TagDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static TagDisplayManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TagDisplayManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new TagDisplayManager(plugin);
        }
    }

    public void loadPlayerTagsConfig(YamlConfiguration playerTagConfig){
        // Reload player tags
        playerTags.clear();
        if(playerTagConfig.contains("tags")){
            for(String tag : playerTagConfig.getConfigurationSection("tags").getKeys(false)) {
                playerTags.put(tag,playerTagConfig.getString("tags." + tag));
            }
        }

        // Load title for the GUI
        if(playerTagConfig.contains("gui-title")) this.GUITitle = playerTagConfig.getString("gui-title");
    }

    public Map<String,String> getAvailableTags(Player player){
        Map<String,String> availableTags = new HashMap<>();
        for(String tagName : playerTags.keySet()){
            if(player.hasPermission("hu.ptag." + tagName)){
                availableTags.put(tagName,playerTags.get(tagName));
            }
        }
        return availableTags;
    }

    public String getGUITitle(){
        return this.GUITitle;
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
                            new Vector3f(1,1,1), // Must have scale or else it wont show
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
