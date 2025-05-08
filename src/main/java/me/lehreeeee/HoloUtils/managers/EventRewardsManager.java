package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.eventrewards.EventReward;
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
    private final Map<String,EventReward> eventRewards = new HashMap<>();
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

        for(String rewardId : rewards.getKeys(false)){

            EventReward reward = new EventReward(rewardId,
                    rewards.getString(rewardId + ".display"),
                    rewards.getString(rewardId + ".skull_texture"),
                    rewards.getStringList(rewardId + ".commands"));
            LoggerUtil.debug("Found reward " + reward);
            eventRewards.put(reward.rewardId(),reward);
        }
    }

    public void giveRewards(String uuid, String rewardId, String server){
        MySQLManager.getInstance().giveEventReward(uuid,rewardId,server);
    }

    public void getRewards(String uuid, Consumer<List<String>> callback){
        MySQLManager.getInstance().getEventRewards(uuid, serverName, callback);
    }

    public boolean claimRewards(Player player, String rewardId, String rowId){
        if(!eventRewards.containsKey(rewardId)){
            player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] This reward is not set up correctly, please report to a developer.",false));
            return false;
        }

        List<String> commands = eventRewards.get(rewardId).commands();

        for(String cmd : commands){
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
        }

        MySQLManager.getInstance().claimEventRewards(rowId);
        return true;
    }

    public String getRewardDisplayName(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        if(reward != null && reward.displayName() != null)
            return reward.displayName();
        else
            return "<gold>" + rewardId;
    }

    public String getRewardSkullTexture(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        if(reward != null && reward.skullTexture() != null)
            return reward.skullTexture();
        else
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIyZDRiZTFhYmNmMzgzMmM5MTYxOTFkMjRmOTYwN2JmMTk0ZWZmOGRmYmYzYjk1MjBiZDk3MjQwZTdjOCJ9fX0=";
    }
}
