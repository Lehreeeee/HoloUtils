package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.utils.LoggerUtil;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventRewardsManager {
    private static EventRewardsManager instance;
    private final Map<String, List<String>> eventRewards = new HashMap<>();
    private String serverName;

    public static EventRewardsManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("EventRewardsManager not initialized.");
        }
        return instance;
    }

    public static void initialize(){
        if (instance == null) {
            instance = new EventRewardsManager();
        }
    }

    public void loadEventRewardsConfig(ConfigurationSection EventRewardsConfig){
        eventRewards.clear();

        serverName = EventRewardsConfig.getString("server_name",null);
        if(serverName == null) LoggerUtil.severe("Server name not found, player will not be able to see any rewards.");

        ConfigurationSection rewards = EventRewardsConfig.getConfigurationSection("rewards");

        for(String key : rewards.getKeys(false)){
            LoggerUtil.debug("Found reward " + key + ", with rewards " + rewards.getStringList(key));
            eventRewards.put(key,rewards.getStringList(key));
        }
    }

    public void giveRewards(String uuid, String rewardId, String server){
        MySQLManager.getInstance().giveEventReward(uuid,rewardId,server);
    }

    public void getRewards(String uuid, Consumer<List<String>> callback){
        MySQLManager.getInstance().getEventRewards(uuid, serverName, callback);
    }

    public void claimRewards(Player player, String rewardId){
        if(!eventRewards.containsKey(rewardId)){
            player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] Reward is not set up correctly, please report to a developer.",false));
            return;
        }

        List<String> commands = eventRewards.get(rewardId);

        for(String cmd : commands){
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
        }
    }
}
