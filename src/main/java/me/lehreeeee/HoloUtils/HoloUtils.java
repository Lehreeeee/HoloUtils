package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.DevChatCommand;
import me.lehreeeee.HoloUtils.commands.DevChatCommandTabCompleter;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommand;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommandTabCompleter;
import me.lehreeeee.HoloUtils.listeners.DisplayListener;
import me.lehreeeee.HoloUtils.listeners.PlayerChatListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
import me.lehreeeee.HoloUtils.managers.DevChatManager;
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
        new PlayerChatListener(this);

        new DisplayListener(this);

        loadCommands();

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
        saveCustomConfig();
        saveDefaultConfig();

        FileConfiguration config = this.getConfig();
        YamlConfiguration playerTitleConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/DisplayTag/PlayerTitles.yml"));
        YamlConfiguration elementalStatusConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/DisplayTag/StatusEffects.yml"));

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        TitleDisplayManager.getInstance().loadPlayerTitlesConfig(playerTitleConfig);
        StatusDisplayManager.getInstance().loadStatusEffectsConfig(elementalStatusConfig);
        DevChatManager.getInstance().setPrefix(config.getString("admin-chat-prefix"));

        // Should print debug msg?
        this.debug = config.getBoolean("debug",false);
    }

    private void saveCustomConfig(){
        File playerTitleFile = new File(this.getDataFolder(), "/DisplayTag/PlayerTitles.yml");
        File elementalStatusFile = new File(this.getDataFolder(), "/DisplayTag/StatusEffects.yml");

        // Create default if not exist
        if(!playerTitleFile.exists() || !elementalStatusFile.exists()) {
            logger.info("Custom config file not found, creating default config.");
            saveResource("DisplayTag/StatusEffects.yml", false);
            saveResource("DisplayTag/PlayerTitles.yml", false);
        }
    }

    private void initializeManagers(){
        logger.info("Initializing TitleDisplayManager...");
        TitleDisplayManager.initialize(this);
        logger.info("Initializing StatusDisplayManager...");
        StatusDisplayManager.initialize(this);
        logger.info("Initializing RedisManager...");
        RedisManager.initialize(logger);
        logger.info("Initializing DevChatManager...");
        DevChatManager.initialize(logger);
    }

    private void loadCommands(){
        logger.info("Loading holoutils commands...");
        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter(this));
        logger.info("Loading adminchat commands...");
        getCommand("devchat").setExecutor(new DevChatCommand(logger));
        getCommand("devchat").setTabCompleter(new DevChatCommandTabCompleter());
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
