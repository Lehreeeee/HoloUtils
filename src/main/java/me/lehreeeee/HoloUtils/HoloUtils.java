package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.*;
import me.lehreeeee.HoloUtils.listeners.DisplayListener;
import me.lehreeeee.HoloUtils.listeners.InventoryListener;
import me.lehreeeee.HoloUtils.listeners.PlayerChatListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
import me.lehreeeee.HoloUtils.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class HoloUtils extends JavaPlugin {
    private final Logger logger = getLogger();
    private PlayerProjectileListener playerProjectileListener;
    private boolean debug = false;
    private boolean fixImmortal = false;
    private boolean enableClaimaccessoriesCommand = false;
    private boolean MMOItemsAvailable = false;

    @Override
    public void onEnable() {
        this.MMOItemsAvailable = Bukkit.getPluginManager().getPlugin("MMOItems") != null;

        // Save the default config.yml
        saveDefaultConfig();
        saveCustomConfig();

        // Wake up the managers
        initializeManagers();

        playerProjectileListener = new PlayerProjectileListener(this);
        new PlayerChatListener(this);
        new DisplayListener(this);
        new InventoryListener(this);

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

        logger.info("Closing MySQL ConnectionPool...");
        MySQLManager.getInstance().closeConnectionPool();

        logger.info("Disabled HoloUtils...");
    }

    public void reloadData(){
        // Make sure files exist
        saveCustomConfig();
        saveDefaultConfig();

        // Custom config files
        YamlConfiguration playerTitleConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/DisplayTag/PlayerTitles.yml"));
        YamlConfiguration elementalStatusConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/DisplayTag/StatusEffects.yml"));
        YamlConfiguration rerollConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/rerolls.yml"));
        YamlConfiguration eventrewardsConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "/EventRewards/rewards.yml"));

        TitleDisplayManager.getInstance().loadPlayerTitlesConfig(playerTitleConfig);
        StatusDisplayManager.getInstance().loadStatusEffectsConfig(elementalStatusConfig);
        EventRewardsManager.getInstance().loadEventRewardsConfig(eventrewardsConfig);
        if(MMOItemsAvailable){
            RerollManager.getInstance().loadRerollConfig(rerollConfig);
        }

        // Base config file
        FileConfiguration config = this.getConfig();

        DevChatManager.getInstance().loadDevChatConfig(config.getConfigurationSection("dev-chat"));
        RedisManager.getInstance().loadRedisConfig(config.getConfigurationSection("redis"));
        MySQLManager.getInstance().loadMySQLConfig(config.getConfigurationSection("mysql"));

        // TODO: To be removed after 3 months
        this.enableClaimaccessoriesCommand = config.getBoolean("enable-claimaccessories-command",false);

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        // Should print debug msg?
        this.debug = config.getBoolean("debug",false);

        // Should plugin check for immortal mob and remove them?
        this.fixImmortal = config.getBoolean("fix-immortal-mob",false);


    }

    public void checkImmortal(Entity entity){
        // Ignore Ender Dragon, or it will force remove before its death animation, making it unkillable edrag
        if(!(entity instanceof LivingEntity livingEntity) || !fixImmortal || livingEntity.getType() == EntityType.ENDER_DRAGON) return;

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

    public static HoloUtils getPlugin(){
        return (HoloUtils) Bukkit.getPluginManager().getPlugin("HoloUtils");
    }

    private void saveCustomConfig(){
        Set<String> customConfigs = Set.of(
                "DisplayTag/PlayerTitles.yml",
                "DisplayTag/StatusEffects.yml",
                "rerolls.yml",
                "EventRewards/rewards.yml"
        );

        File dataFolder = this.getDataFolder();

        for(String path : customConfigs){
            if(!(new File(dataFolder, path).exists())){
                logger.info("Unable to locate " + path + ", creating new one.");
                saveResource(path,false);
            }
        }
    }

    private void initializeManagers(){
        logger.info("Initializing TitleDisplayManager...");
        TitleDisplayManager.initialize(this);
        logger.info("Initializing StatusDisplayManager...");
        StatusDisplayManager.initialize(this);
        logger.info("Initializing RedisManager...");
        RedisManager.initialize();
        logger.info("Initializing DevChatManager...");
        DevChatManager.initialize();
        logger.info("Initializing MySQLManager...");
        MySQLManager.initialize(this);
        logger.info("Initializing EventRewardsManager...");
        EventRewardsManager.initialize();

        if(MMOItemsAvailable){
            logger.info("Found MMOItems, initializing RerollManager");
            RerollManager.initialize();
        }


    }

    private void loadCommands(){
        logger.info("Loading holoutils commands...");
        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter(this));
        logger.info("Loading adminchat commands...");
        getCommand("devchat").setExecutor(new DevChatCommand());
        getCommand("devchat").setTabCompleter(new DevChatCommandTabCompleter());
        logger.info("Loading eventrewards command...");
        getCommand("eventrewards").setExecutor(new EventRewardsCommand());
        getCommand("eventrewards").setTabCompleter(new EventRewardsCommandTabCompleter());

        if(MMOItemsAvailable){
            logger.info("Found MMOItems, loading reroll commands...");
            getCommand("reroll").setExecutor(new RerollCommand(this));
        }

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
