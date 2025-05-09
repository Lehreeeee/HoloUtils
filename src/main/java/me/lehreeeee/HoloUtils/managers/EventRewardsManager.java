package me.lehreeeee.HoloUtils.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.eventrewards.EventReward;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import me.lehreeeee.HoloUtils.utils.SoundUtils;
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

import java.util.*;

public class EventRewardsManager {
    private static EventRewardsManager instance;
    private final Map<String,EventReward> eventRewards = new HashMap<>();
    private final Map<String,List<String>> cachedPlayerRewards = new HashMap<>();
    private String serverName;
    private final int PAGE_SIZE = 28;
    private final NamespacedKey pageNSK = new NamespacedKey("holoutils","page");

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

    public void getAllRewards(String uuid, Inventory inv){
        MySQLManager.getInstance().getAllEventRewards(uuid, serverName, rewards -> {
            if(!rewards.isEmpty()){
                // Cache the rewards once done
                cachedPlayerRewards.put(uuid,rewards);

                // Update Inventory
                updateInventory(uuid,1,inv);
            }
        });
    }

    public void claimRewards(Player player, Inventory inv, String rowId){
        MySQLManager.getInstance().getEventReward(rowId, rewardData -> {
            if(rewardData.isEmpty()) return;

            String[] details = rewardData.split(";");
            String rewardId = details[0];
            String timeGiven = details[1];

            EventReward reward = eventRewards.get(rewardId);
            List<String> commands = reward.commands();

            for(String cmd : commands){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
            }

            MySQLManager.getInstance().updateAllClaimedEventRewards(Set.of(rowId));

            deleteCachedPlayerRewards(String.valueOf(player.getUniqueId()),rewardId,timeGiven,rowId);

            EventRewardsManager.getInstance().updateInventory(String.valueOf(player.getUniqueId()),
                    inv.getItem(49).getItemMeta().getPersistentDataContainer().get(pageNSK, PersistentDataType.INTEGER),
                    inv);
            player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] You have claimed the reward: "
                    + reward.displayName()
                    + ".",false));
        });
    }

    public void claimAllRewards(Player player){
        MySQLManager.getInstance().getAllEventRewards(player.getUniqueId().toString(), serverName, rewards -> {
            if(rewards.isEmpty()) return;

            boolean hasUnclaimable = false;

            Set<String> claimedRowId = new HashSet<>();

            for (String rewardDetails : rewards) {
                String[] details = rewardDetails.split(";");
                String rewardId = details[0];
                String timeGiven = details[1];
                String rowId = details[2];

                if(!eventRewards.containsKey(rewardId)) {
                    hasUnclaimable = true;
                    continue;
                }

                List<String> commands = eventRewards.get(rewardId).commands();

                for(String cmd : commands){
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
                }

                claimedRowId.add(details[2]);

                deleteCachedPlayerRewards(String.valueOf(player.getUniqueId()),rewardId,timeGiven,rowId);
            }

            if(hasUnclaimable){
                SoundUtils.playSound(player,"block.chest.locked");
                player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] 1 or more rewards are not set up correctly, please report to a developer.",false));
            } else {
                SoundUtils.playSound(player,"block.chest.open");
                player.sendMessage(MessageHelper.process("<aqua>[<#FFA500>Event Rewards<aqua>] You have claimed all the rewards.",false));
            }

            if(!claimedRowId.isEmpty())
                MySQLManager.getInstance().updateAllClaimedEventRewards(claimedRowId);
        });
    }

    public void updateInventory(String uuid, int newPage, Inventory inv){

        List<String> allRewards = cachedPlayerRewards.get(uuid);

        // Update page number?
        int totalPages = (int) Math.ceil((double) allRewards.size() / 28);
        LoggerUtils.debug("Taotal Page: " + totalPages);

        // Not last page, update page number
        if(newPage > totalPages){
            LoggerUtils.debug("Last page reached.");
            return;
        } else if(newPage < 1) {
            LoggerUtils.debug("First page reached.");
            return;
        } else {
            ItemMeta claimAllButtonMeta = inv.getItem(49).getItemMeta();
            claimAllButtonMeta.getPersistentDataContainer()
                    .set(new NamespacedKey("holoutils","page"), PersistentDataType.INTEGER, newPage);
            inv.getItem(49).setItemMeta(claimAllButtonMeta);
            LoggerUtils.debug("Page Number: " + newPage);
        }

        int start = (newPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allRewards.size());

        List<String> pageRewards = allRewards.subList(start, end);

        // Clear the page
        for(int i = 10; i <= 43; i++){
            if(i % 9 == 0 || i % 9 == 8) continue;

            inv.setItem(i, null);
        }

        // Repopulate the page
        int rewardSlot = 10;
        for (String rewardDetails : pageRewards) {
            // Make sure only add into reward slot and stop
            while (rewardSlot < 44 && InventoryUtils.isBorderSlot(rewardSlot)) {
                rewardSlot++;
            }

            if (rewardSlot >= 44) break;

            ItemStack rewardItem = createRewardItem(rewardDetails);
            inv.setItem(rewardSlot, rewardItem);
            rewardSlot++;
        }
    }

    public boolean isRewardIdValid(String rewardId){
        return eventRewards.containsKey(rewardId);
    }

    private void deleteCachedPlayerRewards(String uuid, String rewardId, String timeGiven, String rowId){
        String reward = String.join(";",rewardId,timeGiven,rowId);

        cachedPlayerRewards.get(uuid).remove(reward);

        LoggerUtils.debug("Deleted " + uuid + "'s cachedReward: " + reward);
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
