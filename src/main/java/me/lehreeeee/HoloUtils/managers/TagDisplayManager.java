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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
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
    private final Map<UUID,TextDisplay> loadedPlayerTags = new HashMap<>();

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

    public void setDisplayTag(UUID uuid, String tagName){
        Entity targetEntity = Bukkit.getEntity(uuid);
        // Default tag is the tagname
        String tag = tagName;

        // Tag name has value? Replace with the actual tag
        if(playerTags.containsKey(tagName)){
            tag = playerTags.get(tagName);
        }


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

        // Sets the display text
        display.text(MessageHelper.process(tag));

        // Add on top of the entity and make it visible
        targetEntity.addPassenger(display);
        display.setVisibleByDefault(true);

        Bukkit.getScheduler().runTaskLater(plugin, display::remove, 300L);
        debugLogger("Added tag " + tag + " to entity " + uuid + " for 15 sec.");

        // Tracks the loaded tags
        loadedPlayerTags.put(uuid,display);
    }

    public void removeTag(UUID uuid){
        if(!loadedPlayerTags.containsKey(uuid)) return;

        loadedPlayerTags.get(uuid).remove();
        loadedPlayerTags.remove(uuid);

        debugLogger("Removed tag for entity " + uuid);
    }

    public void updateLocation(UUID uuid, Location location){
        debugLogger("Entity teleport event detected.");
        if(!loadedPlayerTags.containsKey(uuid)) return;

        Entity entity = Bukkit.getEntity(uuid);
        TextDisplay tag = loadedPlayerTags.get(uuid);

        if(entity != null){
            // Delay it or it wont work when changing world
            new BukkitRunnable() {
                @Override
                public void run() {
                    tag.teleport(location);
                    entity.addPassenger(tag);
                    debugLogger("Updated tag location for entity " + uuid + " to " + tag.getLocation());
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    public void debugLogger(String debugMessage){
        if(plugin.shouldPrintDebug()) logger.info(debugMessage);
    }
}
