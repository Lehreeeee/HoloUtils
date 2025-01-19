package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.HoloUtilsCommand;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommandTabCompleter;
import me.lehreeeee.HoloUtils.listeners.TitleDisplayListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
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

        TitleDisplayManager.initialize(this);

        playerProjectileListener = new PlayerProjectileListener(this);
        new TitleDisplayListener(this);

        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter(this));

        reloadData();

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        // Remove all loaded tags, just in case... idk if they really persist or not LOL
        TitleDisplayManager.getInstance().removeAllTitles();

        logger.info("Disabled HoloUtils...");
    }

    private void saveCustomConfig(){
        saveResource("DisplayTag/ElementalStatus.yml", false);
        saveResource("DisplayTag/PlayerTitle.yml", false);
    }

    public void reloadData(){
        File playerTitleFile = new File(this.getDataFolder(), "/DisplayTag/PlayerTitle.yml");

        // Create default if not exist
        if(!playerTitleFile.exists()){
            this.saveCustomConfig();
        }

        saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        YamlConfiguration playerTitleConfig = YamlConfiguration.loadConfiguration(playerTitleFile);

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        TitleDisplayManager.getInstance().loadPlayerTitlesConfig(playerTitleConfig);

        // Should print debug msg?
        this.debug = config.getBoolean("debug",false);
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
