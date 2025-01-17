package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.HoloUtilsCommand;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommandTabCompleter;
import me.lehreeeee.HoloUtils.listeners.InventoryInteractionListener;
import me.lehreeeee.HoloUtils.listeners.PlayerProjectileListener;
import me.lehreeeee.HoloUtils.managers.TagDisplayManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;

public final class HoloUtils extends JavaPlugin {

    private final Logger logger = getLogger();
    private PlayerProjectileListener playerProjectileListener;

    @Override
    public void onEnable() {
        // Save the default config.yml
        saveDefaultConfig();
        saveCustomConfig();

        TagDisplayManager.initialize(this);
        playerProjectileListener = new PlayerProjectileListener(this);
        new InventoryInteractionListener(this);

        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter());

        reloadData();

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        logger.info("Disabled HoloUtils...");
    }

    private void saveCustomConfig(){
        saveResource("DisplayTag/ElementalStatus.yml", false);
        saveResource("DisplayTag/PlayerTag.yml", false);
    }

    public void reloadData(){
        File playerTagFile = new File(this.getDataFolder(),"/DisplayTag/PlayerTag.yml");

        // Create default if not exist
        if(!playerTagFile.exists()){
            this.saveCustomConfig();
        }

        FileConfiguration config = this.getConfig();
        YamlConfiguration playerTagConfig = YamlConfiguration.loadConfiguration(playerTagFile);

        playerProjectileListener.setDisabledWorlds(new HashSet<>(config.getStringList("arrow-shoots-thru-players-worlds")));

        TagDisplayManager.getInstance().loadPlayerTagsConfig(playerTagConfig);
    }
}
