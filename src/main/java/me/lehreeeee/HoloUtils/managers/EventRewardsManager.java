package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.utils.LoggerUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<String> getRewards(String uuid){
        return MySQLManager.getInstance().getEventRewards(uuid,serverName);
    }
}
