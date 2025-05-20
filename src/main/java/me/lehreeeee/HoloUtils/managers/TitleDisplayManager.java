package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TitleDisplayManager {
    private static TitleDisplayManager instance;
    private final HoloUtils plugin;

    private String GUIName = "<white>[<aqua>Title Display<white>]";
    private float titleHeight = 0.6F;

    private final Map<String,String> playerTitles = new HashMap<>();
    private final Map<UUID,TextDisplay> loadedPlayerTitles = new HashMap<>();

    private TitleDisplayManager(HoloUtils plugin){
        this.plugin = plugin;
    }

    public static TitleDisplayManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TitleDisplayManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new TitleDisplayManager(plugin);
        }
    }

    public void loadPlayerTitlesConfig(YamlConfiguration playerTitleConfig){
        // Reload player titles
        playerTitles.clear();
        if(playerTitleConfig.contains("titles")){
            for(String title : playerTitleConfig.getConfigurationSection("titles").getKeys(false)) {
                playerTitles.put(title,playerTitleConfig.getString("titles." + title));
            }
        }

        // Load title for the GUI
        if(playerTitleConfig.contains("gui-name")) this.GUIName = playerTitleConfig.getString("gui-name", "<white>[<aqua>Title Display<white>]");

        // Load height for the titles
        if(playerTitleConfig.contains("title-height")) this.titleHeight = (float) playerTitleConfig.getDouble("title-height",0.6);
    }

    public Map<String,String> getAvailableTitles(Player player){
        Map<String,String> availableTitles = new HashMap<>();
        for(String tagName : playerTitles.keySet()){
            if(player.hasPermission("holoutils.playertitle." + tagName)){
                availableTitles.put(tagName, playerTitles.get(tagName));
            }
        }
        return availableTitles;
    }

    public String getGUIName(){
        return this.GUIName;
    }

    public void setTitleDisplay(UUID uuid, String titleName){
        Entity targetEntity = Bukkit.getEntity(uuid);

        if (targetEntity == null) {
            LoggerUtils.warning("Cant find the entity.");
            return;
        }

        if(!(targetEntity instanceof Player)) {
            LoggerUtils.warning("You can't use title display on non-player. (Why tho?)");
            return;
        }

        // Tag name has value? Replace with the actual title
        String title = playerTitles.getOrDefault(titleName, titleName);

        // Does the entity have an existing title already? Remove it first if so
        if(loadedPlayerTitles.containsKey(uuid)){
            // Will be overwritten later
            removeTitle(uuid,true);
        }

        TextDisplay display = spawnDisplayEntity(targetEntity);

        // Sets the display text
        display.text(MessageHelper.process(title));

        // Add on top of the entity and make it visible
        //targetEntity.addPassenger(display);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if(display.isValid()) {
                display.teleport(targetEntity.getLocation().add(0, ((Player) targetEntity).getEyeHeight() + titleHeight,0));
                return;
            }
            task.cancel();
        },1L,1L);

        display.setVisibleByDefault(true);

        // Tracks the loaded titles
        loadedPlayerTitles.put(uuid,display);

        MySQLManager.getInstance().setEquipedTitle(uuid.toString(),titleName);
    }

    public void removeTitle(UUID uuid, boolean skipUnset) {
        TextDisplay display = loadedPlayerTitles.remove(uuid);
        if (display != null) {
            display.remove();
            LoggerUtils.debug("Removed title for entity " + uuid);
        }

        if(skipUnset) return;

        MySQLManager.getInstance().unsetEquipedTitle(uuid.toString());
    }

    public void removeAllTitles(){
        if(loadedPlayerTitles.isEmpty()) return;

        LoggerUtils.info("Found " + loadedPlayerTitles.size() + " remaining player title display, removing them.");
        for(UUID uuid : loadedPlayerTitles.keySet()){
            TextDisplay display = loadedPlayerTitles.get(uuid);
            if(display != null){
                display.remove();
            }
        }

        loadedPlayerTitles.clear();
        LoggerUtils.debug("Removed all loaded titles");
    }

    //TODO: Remove this
    public void updateLocation(UUID uuid, Location location){
        if(!loadedPlayerTitles.containsKey(uuid)) return;

        Entity entity = Bukkit.getEntity(uuid);
        TextDisplay title = loadedPlayerTitles.get(uuid);

        if(entity != null){
            // Delay it or it wont work when changing world
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Teleport is needed after changing world too
                    title.teleport(location);
                    entity.addPassenger(title);
                    LoggerUtils.debug("Updated title location for entity " + uuid + " to " + title.getLocation());
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    public void hideTitle(UUID uuid){
        TextDisplay display = loadedPlayerTitles.get(uuid);
        if(display != null) {
            display.setVisibleByDefault(false);
        }
    }

    public void showTitle(UUID uuid){
        TextDisplay display = loadedPlayerTitles.get(uuid);
        if(display != null) {
            display.setVisibleByDefault(true);
        }
    }

    public void handlePlayerJoin(UUID uuid){
        MySQLManager.getInstance().getUserData(String.valueOf(uuid), data -> {
            if(!data.has("title")) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                setTitleDisplay(uuid, data.get("title").getAsString());
            });
        });
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
                            new Vector3f(0, 0, 0),
                            new AxisAngle4f(), // no left rotation
                            new Vector3f(1,1,1), // Must have scale or else it wont show
                            new AxisAngle4f() // no right rotation
                    )
            );
            entity.setTeleportDuration(3);
        });
    }
}
