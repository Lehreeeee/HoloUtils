package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.*;
import me.lehreeeee.HoloUtils.listeners.DisplayListener;
import me.lehreeeee.HoloUtils.listeners.PlayerChatListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
import me.lehreeeee.HoloUtils.managers.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.logging.Logger;

public final class HoloUtils extends JavaPlugin {

    private final Logger logger = getLogger();
    private PlayerProjectileListener playerProjectileListener;
    private boolean debug = false;
    private boolean fixImmortal = false;
    private boolean enableClaimaccessoriesCommand = false;

    @Override
    public void onEnable() {
        // Save the default config.yml
        saveDefaultConfig();
        saveCustomConfig();

        // Wake up the managers
        initializeManagers();

        playerProjectileListener = new PlayerProjectileListener(this);
        new PlayerChatListener(this);

        new DisplayListener(this);

        loadCommands();

        reloadData();

        // Welcome to my yt channel, please saracabribe
        RedisManager.getInstance().subscribe();

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        // Remove all loaded tags, just in case... idk if they really persist or not LOL
        logger.info("Removing remaining player title display...");
        TitleDisplayManager.getInstance().removeAllTitles();
        logger.info("Removing remaining status effect display...");
        StatusDisplayManager.getInstance().removeAllStatusDisplay();
        MySQLManager.getInstance().closeConnectionPool();

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
        DevChatManager.getInstance().loadDevChatConfig(config.getConfigurationSection("dev-chat"));
        RedisManager.getInstance().loadRedisConfig(config.getConfigurationSection("redis"));

        // TODO: To be removed after 3 months
        this.enableClaimaccessoriesCommand = config.getBoolean("enable-claimaccessories-command",false);
        if(enableClaimaccessoriesCommand){
            MySQLManager.getInstance().loadMySQLConfig(config.getConfigurationSection("mysql"));
        }

        // Should print debug msg?
        this.debug = config.getBoolean("debug",false);

        // Should plugin check for immortal mob and remove them?
        this.fixImmortal = config.getBoolean("fix-immortal-mob",false);


    }

    public void checkImmortal(Entity entity){
        if(!(entity instanceof LivingEntity livingEntity) || !fixImmortal) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (livingEntity.getHealth() > 0) {
                    logger.info(MessageFormat.format("Entity {0} or {1} is still alive after death event, force removing.",
                            entity.getUniqueId(),entity.getName()));
                    entity.remove();
                }
            }
        }.runTaskLater(this, 1L);
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

        // TODO: To be removed after 3 months
        if(enableClaimaccessoriesCommand){
            logger.info("Initializing MySQLManager...");
            MySQLManager.initialize(this);
        }
    }

    private void loadCommands(){
        logger.info("Loading holoutils commands...");
        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter(this));
        logger.info("Loading adminchat commands...");
        getCommand("devchat").setExecutor(new DevChatCommand(logger));
        getCommand("devchat").setTabCompleter(new DevChatCommandTabCompleter());

        // TODO: To be removed after 3 months
        if(enableClaimaccessoriesCommand){
            logger.info("Loading claimaccessories commands...");
            getCommand("claimaccessories").setExecutor(new ClaimAccessoriesCommand(logger));
        }
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
