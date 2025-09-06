package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.*;
import me.lehreeeee.HoloUtils.managers.FileRotaterManager;
import me.lehreeeee.HoloUtils.hooks.PlaceholderAPIHook;
import me.lehreeeee.HoloUtils.listeners.*;
import me.lehreeeee.HoloUtils.managers.*;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
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

public final class HoloUtils extends JavaPlugin {
    public static HoloUtils plugin;
    public static boolean debug = false;

    private PlayerProjectileListener playerProjectileListener;
    private boolean fixImmortal = false;
    private boolean enableClaimaccessoriesCommand = false;
    private boolean MMOItemsAvailable = false;
    private String serverName;

    @Override
    public void onEnable() {
        plugin = this;

        this.MMOItemsAvailable = Bukkit.getPluginManager().getPlugin("MMOItems") != null;

        // Save the default config.yml
        saveDefaultConfig();
        saveCustomConfig();

        // Wake up the managers
        initializeManagers();

        initializeListeners();

        loadCommands();

        reloadData();

        // Welcome to my yt channel, please saracabribe
        RedisManager.getInstance().subscribe();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook().register();
        }

        LoggerUtils.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        // Remove all loaded tags, just in case... idk if they really persist or not LOL
        LoggerUtils.info("Removing remaining player title display...");
        TitleDisplayManager.getInstance().removeAllTitles();
        LoggerUtils.info("Removing remaining status effect display...");
        StatusDisplayManager.getInstance().removeAllStatusDisplay();

        LoggerUtils.info("Closing MySQL ConnectionPool...");
        MySQLManager.getInstance().closeConnectionPool();

        LoggerUtils.info("Disabled HoloUtils...");
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

        // Base config file
        FileConfiguration config = this.getConfig();

        DevChatManager.getInstance().loadDevChatConfig(config.getConfigurationSection("dev-chat"));
        RedisManager.getInstance().loadRedisConfig(config.getConfigurationSection("redis"));
        MySQLManager.getInstance().loadMySQLConfig(config.getConfigurationSection("mysql"));

        if(MMOItemsAvailable){
            RerollManager.getInstance().loadRerollConfig(rerollConfig);
            DeconstructorManager.getInstance().loadDeconstructorConfig(config.getConfigurationSection("deconstructor"));
        }

        // TODO: To be removed after 3 months
        // -git blame @Lehreeeee the server is on fire!!!
        this.enableClaimaccessoriesCommand = config.getBoolean("enable-claimaccessories-command",false);

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        // Should print debug log?
        debug = config.getBoolean("debug",false);

        // Should plugin check for immortal mob and remove them?
        fixImmortal = config.getBoolean("fix-immortal-mob",false);

        // FileRotater on?
        if(config.getBoolean("auto-rotate-files",false)) {
            FileRotaterManager.getInstance().start();
        }

        // Set the server the plugin is at
        serverName = config.getString("server_name",null);
        if(serverName == null || serverName.isBlank()) {
            LoggerUtils.severe("Server name not found, some features may not work properly.");
        }
    }

    public void checkImmortal(Entity entity){
        // Ignore Ender Dragon, or it will force remove before its death animation, making it unkillable edrag
        if(!(entity instanceof LivingEntity livingEntity) || !fixImmortal || livingEntity.getType() == EntityType.ENDER_DRAGON) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (livingEntity.getHealth() > 0) {
                    LoggerUtils.info(MessageFormat.format("Entity {0} or {1} is still alive after death event, force removing.",
                            entity.getUniqueId(),entity.getName()));
                    entity.remove();
                }
            }
        }.runTaskLater(this, 1L);
    }

    public String getServerName(){
        if(serverName == null || serverName.isBlank()) {
            LoggerUtils.severe("Server name not found, some features may not work properly.");
            return "";
        }
        return serverName;
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
                LoggerUtils.info("Unable to locate " + path + ", creating new one.");
                saveResource(path,false);
            }
        }
    }

    private void initializeManagers(){
        LoggerUtils.info("Initializing TitleDisplayManager...");
        TitleDisplayManager.initialize(this);
        LoggerUtils.info("Initializing StatusDisplayManager...");
        StatusDisplayManager.initialize(this);
        LoggerUtils.info("Initializing RedisManager...");
        RedisManager.initialize();
        LoggerUtils.info("Initializing DevChatManager...");
        DevChatManager.initialize();
        LoggerUtils.info("Initializing MySQLManager...");
        MySQLManager.initialize(this);
        LoggerUtils.info("Initializing EventRewardsManager...");
        EventRewardsManager.initialize();
        LoggerUtils.info("Initializing FileRotaterManager...");
        FileRotaterManager.initialize(this);
        LoggerUtils.info("Initializing AutoCaptchaManager...");
        AutoCaptchaManager.initialize(this);

        if(MMOItemsAvailable){
            LoggerUtils.info("Found MMOItems, initializing RerollManager and DeconstructorManager");
            RerollManager.initialize();
            DeconstructorManager.initialize();
        }
    }

    private void initializeListeners(){
        playerProjectileListener = new PlayerProjectileListener(this);
        Bukkit.getPluginManager().registerEvents(playerProjectileListener,this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(),this);
        Bukkit.getPluginManager().registerEvents(new DisplayListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(),this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerFishListener(this), this);

        if(Bukkit.getPluginManager().getPlugin("CMI") != null){
            LoggerUtils.info("Found CMI, initializing CMIListener");
            Bukkit.getPluginManager().registerEvents(new CMIListener(),this);
        }
    }


    private void loadCommands(){
        LoggerUtils.info("Loading holoutils commands...");
        getCommand("holoutils").setExecutor(new HoloUtilsCommand());
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter());
        LoggerUtils.info("Loading adminchat commands...");
        getCommand("devchat").setExecutor(new DevChatCommand());
        getCommand("devchat").setTabCompleter(new DevChatCommandTabCompleter());
        LoggerUtils.info("Loading eventrewards command...");
        getCommand("eventrewards").setExecutor(new EventRewardsCommand());
        getCommand("eventrewards").setTabCompleter(new EventRewardsCommandTabCompleter());
        LoggerUtils.info("Loading playertitle command...");
        getCommand("playertitle").setExecutor(new PlayerTitleCommand());

        if(MMOItemsAvailable){
            LoggerUtils.info("Found MMOItems, loading reroll and deconstructor commands...");
            getCommand("reroll").setExecutor(new RerollCommand());
            getCommand("deconstructor").setExecutor(new DeconstructorCommand());
        }

        // TODO: To be removed after 3 months
        if(enableClaimaccessoriesCommand){
            LoggerUtils.info("Loading claimaccessories commands...");
            getCommand("claimaccessories").setExecutor(new ClaimAccessoriesCommand());
        }
    }
}

//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//
