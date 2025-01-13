package me.lehreeeee.HoloUtils;

import me.lehreeeee.HoloUtils.commands.HoloUtilsCommand;
import me.lehreeeee.HoloUtils.commands.HoloUtilsCommandTabCompleter;
import me.lehreeeee.HoloUtils.listeners.EntityDamageListener;
import me.lehreeeee.HoloUtils.managers.TextDisplayManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class HoloUtils extends JavaPlugin {

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        getCommand("holoutils").setExecutor(new HoloUtilsCommand(this, new TextDisplayManager(this)));
        getCommand("holoutils").setTabCompleter(new HoloUtilsCommandTabCompleter());

        new EntityDamageListener(this);

        logger.info("Enabled HoloUtils...");
    }

    @Override
    public void onDisable() {
        logger.info("Disabled HoloUtils...");
    }
}
