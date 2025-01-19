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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.logging.Logger;

public class StatusDisplayManager {
    private static StatusDisplayManager instance;
    private final HoloUtils plugin;
    private final Logger logger;
    private float statusHeight = 0.6F;
    private final Map<String,String> statusEffects = new HashMap<>();
    private final Map<UUID,TextDisplay> loadedStatusDisplay = new HashMap<>();
    // String/Key is [uuid];[status_effect]
    private final Map<String, BukkitTask> scheduledTasks = new HashMap<>();

    private StatusDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static StatusDisplayManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("StatusDisplayManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new StatusDisplayManager(plugin);
        }
    }

    public List<String> getAvailableStatuses(){
        return new ArrayList<>(statusEffects.keySet());
    }

    public void loadStatusEffectsConfig(YamlConfiguration statusEffectsConfig){
        statusEffects.clear();
        if(statusEffectsConfig.contains("status-effects")){
            for(String effects : statusEffectsConfig.getConfigurationSection("status-effects").getKeys(false)) {
                statusEffects.put(effects,statusEffectsConfig.getString("status-effects." + effects));
            }
        }

        // Load height for the status
        if(statusEffectsConfig.contains("status-height")) this.statusHeight = (float) statusEffectsConfig.getDouble("status-height",0.6);
    }

    public void setStatusDisplay(UUID uuid, String element, Long tick){
        Entity targetEntity = Bukkit.getEntity(uuid);

        if (targetEntity == null) {
            logger.warning("Cant find the entity.");
            return;
        }

        if(targetEntity instanceof Player) {
            logger.warning("You can't use status display on player. (yet?)");
            return;
        }

        // Default status symbol
        String status = "<red>?";

        if(statusEffects.containsKey(element)){
            status = statusEffects.get(element);
        }

        // Has existing status display? Append it if yes
        if(loadedStatusDisplay.containsKey(uuid)){
            debugLogger("Found existing display, appending to it.");
            TextDisplay display = loadedStatusDisplay.get(uuid);
            String currentString = MessageHelper.revert(display.text());

            // If exists, remove first
            if(currentString.contains(status) && scheduledTasks.containsKey(uuid + element)) {
                debugLogger("Found existing same element, removing before applying new one.");

                // Cancel the scheduled task and remove from the list
                scheduledTasks.get(uuid + element).cancel();
                scheduledTasks.remove(uuid + element);

                // Remove element but keep the display because we adding new 1
                removeStatus(uuid,display,element,true);

                // Update current string
                currentString = MessageHelper.revert(display.text());
            }

            // Add the status
            display.text(MessageHelper.process(currentString + status));

            // Schedule for removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeStatus(uuid, display, element,false);
            }, tick);

            scheduledTasks.put(uuid + element , task);
        } else {
            debugLogger("Can't find existing display, spawning new one.");

            TextDisplay display = spawnDisplayEntity(targetEntity);

            // Sets the display text
            display.text(MessageHelper.process(status));

            // Add on top of the entity and make it visible
            targetEntity.addPassenger(display);
            display.setVisibleByDefault(true);

            // Schedule for removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeStatus(uuid, display, element,false);
            }, tick);

            scheduledTasks.put(uuid + element, task);
            loadedStatusDisplay.put(uuid,display);
        }
    }

    private void removeStatus(UUID uuid, TextDisplay display, String status, boolean keepDisplay){
        if(display == null){
            logger.warning("Display doko?");
            return;
        }

        debugLogger("Removing status " + status + " for " + uuid);
        String currentString = MessageHelper.revert(display.text());
        String updatedString = currentString;

        String symbol = statusEffects.get(status);

        // Remove from the string
        if(symbol != null){
            updatedString = currentString.replaceFirst(symbol ,"");
        }

        debugLogger("Old String - " + currentString);
        debugLogger("New String - " + updatedString);

        // If empty/it was the last status, remove it completely
        if(updatedString.isBlank() && !keepDisplay){
            display.remove();
            loadedStatusDisplay.remove(uuid);
            return;
        }

        // Update display
        display.text(MessageHelper.process(updatedString));
    }

    private TextDisplay spawnDisplayEntity(Entity targetEntity){
        // Spawn the display entity
        World world = targetEntity.getWorld();
        Location location = targetEntity.getLocation();

        return world.spawn(location, TextDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.setBillboard(Display.Billboard.CENTER);

            entity.setVisibleByDefault(false);
            entity.setTransformation(
                    new Transformation(
                            new Vector3f(0, statusHeight, 0),
                            new AxisAngle4f(), // no left rotation
                            new Vector3f(1,1,1), // Must have scale or else it wont show
                            new AxisAngle4f() // no right rotation
                    )
            );
        });
    }

    private void debugLogger(String debugMessage){
        if(plugin.shouldPrintDebug()) logger.info(debugMessage);
    }
}
