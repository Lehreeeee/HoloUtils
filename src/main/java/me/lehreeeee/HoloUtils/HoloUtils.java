package me.lehreeeee.HoloUtils;

import jdk.jshell.Snippet;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommand;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommandTabCompleter;
import me.lehreeeee.HoloUtils.listeners.DisplayListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
import me.lehreeeee.HoloUtils.managers.RedisManager;
import me.lehreeeee.HoloUtils.managers.StatusDisplayManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;

public final class HoloUtils extends JavaPlugin {

    private final Logger logger = getLogger();
    private PlayerProjectileListener playerProjectileListener;
    private boolean debug = false;

    @Override
    public void onEnable() {
        // Save the default config.yml
        saveDefaultConfig();
        saveCustomConfig();

        // Wake up the managers
        initializeManagers();

        // Welcome to my yt channel, please saracabribe
        RedisManager.getInstance().subscribe();

        playerProjectileListener = new PlayerProjectileListener(this);
        new DisplayListener(this);

        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter(this));

        reloadData();

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        // Remove all loaded tags, just in case... idk if they really persist or not LOL
        logger.info("Removing remaining player title display...");
        TitleDisplayManager.getInstance().removeAllTitles();
        logger.info("Removing remaining status effect display...");
        StatusDisplayManager.getInstance().removeAllStatusDisplay();

        logger.info("Disabled HoloUtils...");
    }

    public void reloadData(){
        File playerTitleFile = new File(this.getDataFolder(), "/DisplayTag/PlayerTitles.yml");
        File elementalStatusFile = new File(this.getDataFolder(), "/DisplayTag/StatusEffects.yml");

        // Create default if not exist
        if(!playerTitleFile.exists() || !elementalStatusFile.exists()){
            this.saveCustomConfig();
        }

        saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        YamlConfiguration playerTitleConfig = YamlConfiguration.loadConfiguration(playerTitleFile);
        YamlConfiguration elementalStatusConfig = YamlConfiguration.loadConfiguration(elementalStatusFile);

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        TitleDisplayManager.getInstance().loadPlayerTitlesConfig(playerTitleConfig);
        StatusDisplayManager.getInstance().loadStatusEffectsConfig(elementalStatusConfig);

        // Should print debug msg?
        this.debug = config.getBoolean("debug",false);
    }

    private void saveCustomConfig(){
        saveResource("DisplayTag/StatusEffects.yml", false);
        saveResource("DisplayTag/PlayerTitles.yml", false);
    }

    private void initializeManagers(){
        logger.info("Initializing TitleDisplayManager...");
        TitleDisplayManager.initialize(this);
        logger.info("Initializing StatusDisplayManager...");
        StatusDisplayManager.initialize(this);
        logger.info("Initializing RedisManager...");
        RedisManager.initialize(this);
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
