package me.lehreeeee.HoloUtils.managers;

//import com.ticxo.modelengine.api.ModelEngineAPI;
//import com.ticxo.modelengine.api.model.ActiveModel;
//import com.ticxo.modelengine.api.model.bone.manager.MountManager;
//import com.ticxo.modelengine.api.mount.controller.MountControllerTypes;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.hooks.ModelEngineHook;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class StatusDisplayManager {
    private static StatusDisplayManager instance;
    private final HoloUtils plugin;

    private float statusHeight = 0.6F;
    private final boolean modelEngineAvailable;

    private final Map<String,String> statusEffects = new HashMap<>();
    private final Map<UUID,TextDisplay> loadedStatusDisplay = new HashMap<>();
    private final Map<String, BukkitTask> scheduledTasks = new HashMap<>();

    private StatusDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
        this.modelEngineAvailable = Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
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

    public void setStatusDisplay(UUID uuid, String status, Long tick){
        Entity targetEntity = Bukkit.getEntity(uuid);

        if (targetEntity == null) {
            LoggerUtils.warning("Cant find the entity.");
            return;
        }

        if(targetEntity instanceof Player) {
            LoggerUtils.warning("You can't use status display on player. (yet?)");
            return;
        }

        // Default status symbol
        String statusSymbol = "<white>\uD83D\uDD0E";

        if(statusEffects.containsKey(status)){
            statusSymbol = statusEffects.get(status);
        }

        // Has existing status display? Append it if yes
        if(loadedStatusDisplay.containsKey(uuid)){
            LoggerUtils.debug("Found existing display, appending to it.");
            TextDisplay display = loadedStatusDisplay.get(uuid);
            String currentString = MessageUtils.revert(display.text());

            // If exists, remove first
            if(scheduledTasks.containsKey(uuid + ";" + status)) {
                LoggerUtils.debug("Found existing same status, removing before applying new one.");

                // Cancel the scheduled task and remove from the list
                scheduledTasks.get(uuid + ";" + status).cancel();

                // Remove status but keep the display because we adding new 1 later
                removeStatus(uuid,display,status,true);

                // Update current string
                currentString = MessageUtils.revert(display.text());
            }

            // Add the status
            display.text(MessageUtils.process(currentString + statusSymbol));

            // Schedule for removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeStatus(uuid, display, status,false);
            }, tick);

            scheduledTasks.put(uuid + ";" + status, task);
        } else {
            LoggerUtils.debug("Can't find existing display, spawning new one.");

            TextDisplay display = spawnDisplayEntity(targetEntity);

            // Sets the display text
            display.text(MessageUtils.process(statusSymbol));

            // Add on top of the entity and make it visible
            mountDisplay(targetEntity,display);

            // Schedule for removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeStatus(uuid, display, status,false);
            }, tick);

            scheduledTasks.put(uuid + ";" + status, task);
            loadedStatusDisplay.put(uuid,display);
        }
    }

    public void updateLocation(UUID uuid, Location location){
        if(!loadedStatusDisplay.containsKey(uuid)) return;

        Entity entity = Bukkit.getEntity(uuid);
        TextDisplay display = loadedStatusDisplay.get(uuid);

        if(entity != null){
            // Delay it or it wont work when changing world
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Teleport is needed after changing world too
                    display.teleport(location);
                    mountDisplay(entity,display);
                    LoggerUtils.debug("Updated title location for entity " + uuid + " to " + display.getLocation());
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    public void removeStatusDisplay(UUID uuid){
        if(!loadedStatusDisplay.containsKey(uuid)) return;

        TextDisplay display = loadedStatusDisplay.get(uuid);
        if(display != null){
            display.remove();
        }

        loadedStatusDisplay.remove(uuid);
        LoggerUtils.debug("Removed status display for entity " + uuid);
    }

    public void removeAllStatusDisplay(){
        if(loadedStatusDisplay.isEmpty()) return;

        LoggerUtils.info("Found " + loadedStatusDisplay.size() + " remaining status effect display, removing them.");
        for(UUID uuid : loadedStatusDisplay.keySet()){
            TextDisplay display = loadedStatusDisplay.get(uuid);
            if(display != null){
                display.remove();
            }
        }

        loadedStatusDisplay.clear();
        LoggerUtils.debug("Removed all loaded status display");
    }

    private void mountDisplay(Entity targetEntity, TextDisplay display){

        // Use the basic mount if its not a modeled entity.
        if (!modelEngineAvailable || !ModelEngineHook.isModeledEntity(targetEntity.getUniqueId())) {
            targetEntity.addPassenger(display);
            display.setVisibleByDefault(true);
            return;
        }

        LoggerUtils.debug("This entity is a Modeled Entity.");
        ModelEngineHook.mountStatusDisplay(targetEntity,display);
    }

    private void removeStatus(UUID uuid, TextDisplay display, String status, boolean keepDisplay){
        if(display == null){
            LoggerUtils.debug("Display not found for " + uuid + ", skipping.");
            return;
        }

        LoggerUtils.debug("Removing status " + status + " for " + uuid);
        scheduledTasks.remove(uuid + ";" + status);

        String updatedString = getLatestStatusEffectString(uuid);
        LoggerUtils.debug("New String - " + updatedString);

        // If empty/it was the last status, remove it completely
        if(updatedString.isBlank() && !keepDisplay){
            display.remove();
            loadedStatusDisplay.remove(uuid);
            return;
        }

        // Update display
        display.text(MessageUtils.process(updatedString));
    }

    private String getLatestStatusEffectString(UUID uuid){
        // Define a StringBuilder to construct the resulting string
        StringBuilder effectsBuilder = new StringBuilder();

        // Iterate through the statusEffects map to construct keys and fetch symbols
        for (Map.Entry<String, String> entry : statusEffects.entrySet()) {
            String effectName = entry.getKey();  // Effect name is the key
            String symbol = entry.getValue();   // Effect symbol is the value

            // Construct the key using the UUID and the effect name
            String taskKey = uuid.toString() + ";" + effectName;

            // Check if this key exists in the scheduledTasks map
            if (scheduledTasks.containsKey(taskKey)) {
                // If the task exists, append the symbol to the effectsBuilder
                effectsBuilder.append(symbol);
            }
        }

        // Return the final string of concatenated symbols
        return effectsBuilder.toString();
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
}
