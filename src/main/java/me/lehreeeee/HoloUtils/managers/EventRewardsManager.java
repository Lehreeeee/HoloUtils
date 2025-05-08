package me.lehreeeee.HoloUtils.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.eventrewards.EventReward;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        if(serverName == null) LoggerUtils.severe("Server name not found, player will not be able to see any rewards.");

        ConfigurationSection rewards = EventRewardsConfig.getConfigurationSection("rewards");

        for(String rewardId : rewards.getKeys(false)){

            EventReward reward = new EventReward(rewardId,
                    rewards.getString(rewardId + ".display"),
                    rewards.getString(rewardId + ".skull_texture"),
                    rewards.getStringList(rewardId + ".commands"));
            LoggerUtils.debug("Found reward " + reward);
            eventRewards.put(reward.rewardId(),reward);
        }
    }

    public void giveRewards(String uuid, String rewardId, String server){
        MySQLManager.getInstance().giveEventReward(uuid,rewardId,server);
    }

    public void getRewards(String uuid, int page, Inventory inv){
        ItemMeta claimAllButtonMeta = inv.getItem(49).getItemMeta();

        MySQLManager.getInstance().getEventRewards(uuid, page, serverName, rewards -> {

            // Update page number if not empty, will be ignored if empty
            if(!rewards.isEmpty()){
                claimAllButtonMeta.getPersistentDataContainer()
                        .set(new NamespacedKey("holoutils","page"), PersistentDataType.INTEGER, page);
                inv.getItem(49).setItemMeta(claimAllButtonMeta);

                // Clear all item
                for(int i = 10; i <= 43; i++){
                    if(i % 9 == 0 || i % 9 == 8) continue;

                    inv.setItem(i, null);
                }
            }

            int rewardSlot = 10;
            for (String rewardDetails : rewards) {
                // Make sure only add into reward slot and stop
                while (rewardSlot < 44 && InventoryUtils.isBorderSlot(rewardSlot)) {
                    rewardSlot++;
                }

                // TODO: Add more pages for more than 28 rewards
                if (rewardSlot >= 44) break;

                ItemStack rewardItem = createRewardItem(rewardDetails);
                inv.setItem(rewardSlot, rewardItem);
                rewardSlot++;
            }
        });
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

    private ItemStack createRewardItem(String rewardDetails){
        String[] details = rewardDetails.split(";");
        String rewardId = details[0];
        String timeStamp = details[1];
        String rowId = details[2];

        ItemStack rewardHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) rewardHead.getItemMeta();

        if(skullMeta != null){
            skullMeta.displayName(MessageHelper.process(getRewardDisplayName(rewardId)));
            String base64 = getRewardSkullTexture(rewardId);

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add((new ProfileProperty("textures", base64)));
            skullMeta.setPlayerProfile(profile);

            skullMeta.lore(List.of(
                    MessageHelper.process("<blue>Time Received: <green>" + timeStamp + " GMT+8")
            ));

            PersistentDataContainer skullPDC = skullMeta.getPersistentDataContainer();
            skullPDC.set(new NamespacedKey("holoutils","rewardid"), PersistentDataType.STRING, rewardId);
            skullPDC.set(new NamespacedKey("holoutils","rowid"), PersistentDataType.STRING, rowId);

            rewardHead.setItemMeta(skullMeta);
        }

        return rewardHead;
    }

    private String getRewardDisplayName(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        if(reward != null && reward.displayName() != null)
            return reward.displayName();
        else
            return "<gold>" + rewardId;
    }

    private String getRewardSkullTexture(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        if(reward != null && reward.skullTexture() != null)
            return reward.skullTexture();
        else
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIyZDRiZTFhYmNmMzgzMmM5MTYxOTFkMjRmOTYwN2JmMTk0ZWZmOGRmYmYzYjk1MjBiZDk3MjQwZTdjOCJ9fX0=";
    }
}
