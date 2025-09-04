package me.lehreeeee.HoloUtils.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoCaptchaManager {
    private JavaPlugin plugin;
    private static AutoCaptchaManager instance;
    private int fishingOdds;
    private int mobKillOdds;
    private String captchaCheckCommand;

    public AutoCaptchaManager(JavaPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        fishingOdds = config.getInt("AutoCaptcha.Fishing");
        mobKillOdds = config.getInt("AutoCaptcha.MobKills");
        captchaCheckCommand = config.getString("AutoCaptcha.CommandExecuted");

    }

    public static AutoCaptchaManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FileRotater not initialized.");
        }
        return instance;
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new AutoCaptchaManager(plugin);
        }
    }

    public void executeFishCheck(String playerName) {
        generateRandomBooleanWithOdds(playerName, fishingOdds);

    }

    public void executeMobKillCheck(String playerName) {
        generateRandomBooleanWithOdds(playerName, mobKillOdds);
    }

    //This is a horrendous name
    private void generateRandomBooleanWithOdds(String playerName, int odds) {
        if (odds < 1) {
            return;
        }
        int numberReturned = (int) (Math.random() * odds);
        //Bukkit.getLogger().info(playerName + " has been triggered captcha: " + numberReturned);

        if(numberReturned == 0) {
            String command = captchaCheckCommand.replace("%playername%", playerName);
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

}


