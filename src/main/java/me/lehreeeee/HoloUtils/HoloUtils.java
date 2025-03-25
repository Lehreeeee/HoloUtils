package me.lehreeeee.HoloUtils;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
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

        //registerMythicMobsPlaceholders();

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        // Remove all loaded tags, just in case... idk if they really persist or not LOL
        logger.info("Removing remaining player title display...");
        TitleDisplayManager.getInstance().removeAllTitles();
        logger.info("Removing remaining status effect display...");
        StatusDisplayManager.getInstance().removeAllStatusDisplay();

        if(enableClaimaccessoriesCommand){
            MySQLManager.getInstance().closeConnectionPool();
        }

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

        TitleDisplayManager.getInstance().loadPlayerTitlesConfig(playerTitleConfig);
        StatusDisplayManager.getInstance().loadStatusEffectsConfig(elementalStatusConfig);
        if(MMOItemsAvailable){
            RerollManager.getInstance().loadRerollConfig(rerollConfig);
        }

        // Base config file
        FileConfiguration config = this.getConfig();

        DevChatManager.getInstance().loadDevChatConfig(config.getConfigurationSection("dev-chat"));
        RedisManager.getInstance().loadRedisConfig(config.getConfigurationSection("redis"));
        // TODO: To be removed after 3 months
        this.enableClaimaccessoriesCommand = config.getBoolean("enable-claimaccessories-command",false);
        if(enableClaimaccessoriesCommand){
            MySQLManager.getInstance().loadMySQLConfig(config.getConfigurationSection("mysql"));
        }

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
                "rerolls.yml"
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

        if(MMOItemsAvailable){
            logger.info("Found MMOItems, initializing RerollManager");
            RerollManager.initialize();
        }

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

    private void registerMythicMobsPlaceholders(){
        if(Bukkit.getPluginManager().getPlugin("MythicMobs") == null){
            logger.info("MythicMobs not found, not registering custom placeholders.");
            return;
        }

        MythicBukkit.inst().getPlaceholderManager().register("hu.mmodamage", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            final SkillMetadata skillMeta = (SkillMetadata) metadata;
            final Optional<AbstractEntity> damagedOpt = skillMeta.getEntityTargets().stream().findFirst();
            //Validate.isTrue(damagedOpt.isPresent(), "Could not find target entity");
            final Entity attacker = skillMeta.getCaster().getEntity().getBukkitEntity();
            final Entity damaged = damagedOpt.get().getBukkitEntity();

            logger.info("Attacker-Damaged " + attacker.getName() + damaged.getName());

            logger.info("Going thru targets");
            for(AbstractEntity entity : skillMeta.getEntityTargets()){
                logger.info(entity.getBukkitEntity().getName());
            }
            logger.info("Trigger - " + skillMeta.getTrigger().getName());
            logger.info("Caster - " + skillMeta.getCaster().getName());

            final AttackMetadata attackMeta = MythicLib.plugin.getDamage().findAttack(new EntityDamageEvent(damaged, EntityDamageEvent.DamageCause.CUSTOM, 0));
            //Validate.notNull(attackMeta, "Entity not being attacked");

            if (arg != null && !arg.isEmpty()) {
                for (Element element : Element.values()) {
                    if(element.getName().equalsIgnoreCase(arg)) {
                        logger.info("Found element - " + element.getName());
                        logger.info("Damage - " + attackMeta.getDamage().getDamage(element));
                        logger.info(attackMeta.getDamage().toString());
                        return String.valueOf(attackMeta.getDamage().getDamage(element));
                    }
                }

                final DamageType type = DamageType.valueOf(UtilityMethods.enumName(arg));
                return String.valueOf(attackMeta.getDamage().getDamage(type));
            }
            return String.valueOf(attackMeta.getDamage().getDamage());
        }));
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
