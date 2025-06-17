package me.lehreeeee.HoloUtils.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.eventrewards.EventReward;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import me.lehreeeee.HoloUtils.utils.SoundUtils;
import net.kyori.adventure.text.Component;
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
        if(serverName == null) LoggerUtils.warning("Server name not found, player will not be able to see any rewards.");

        ConfigurationSection rewards = EventRewardsConfig.getConfigurationSection("rewards");

        for(String rewardId : rewards.getKeys(false)){
            EventReward reward = new EventReward(rewardId,
                    rewards.getString(rewardId + ".display"),
                    rewards.getStringList(rewardId + ".lore"),
                    rewards.getString(rewardId + ".skull_texture"),
                    rewards.getStringList(rewardId + ".commands"));
            eventRewards.put(reward.rewardId(),reward);
        }
    }

    public void giveRewards(String uuid, String rewardId, String servers){
        Arrays.stream(servers.split(",")).toList().forEach((server) -> {
            MySQLManager.getInstance().giveEventReward(uuid,rewardId,server);
        });

        LoggerUtils.file("EventRewards", "Gave reward " + rewardId + " to "
                + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() + " for server(s) " + servers + ".");
    }

    // Query all rewards and populate the inventory when first open /eventrewards claim
    public void getAllRewards(String uuid, Inventory inv){
        MySQLManager.getInstance().getAllEventRewards(uuid, serverName, rewards -> {
            if(!rewards.isEmpty()){
                LoggerUtils.file("EventRewards","Player " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()
                        + " opened claim GUI with " + rewards.size() + " rewards." );

                // Cache the rewards once done
                cachedPlayerRewards.put(uuid,rewards);

                // Update Inventory
                updateInventory(uuid,1,inv);
            }
        });
    }

    public void claimReward(Player player, Inventory inv, String rowId){
        String uuid = player.getUniqueId().toString();
        MySQLManager.getInstance().getEventReward(rowId, rewardData -> {
            if(rewardData.isEmpty()) return;

            String[] details = rewardData.split(";");
            String rewardId = details[1];

            EventReward reward = eventRewards.get(rewardId);
            List<String> commands = reward.commands();

            for(String cmd : commands){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
            }

            // Update database and cache
            MySQLManager.getInstance().updateAllClaimedEventRewards(Set.of(rowId));
            cachedPlayerRewards.get(uuid).remove(rewardData);
            LoggerUtils.debug("Deleted " + uuid + "'s cachedReward: " + reward);

            // Refresh Inventory
            EventRewardsManager.getInstance().updateInventory(uuid,
                    inv.getItem(49).getItemMeta().getPersistentDataContainer().get(pageNSK, PersistentDataType.INTEGER),
                    inv);

            player.sendMessage(MessageUtils.process("<aqua>[<#FFA500>Event Rewards<aqua>] You have claimed the reward: "
                    + reward.displayName()
                    + ".",false));

            LoggerUtils.file("EventRewards",player.getName() + " claimed rewards: " + rewardData);
        });
    }

    public void claimAllRewards(Player player){
        String uuid = player.getUniqueId().toString();
        MySQLManager.getInstance().getAllEventRewards(uuid, serverName, rewards -> {
            if(rewards.isEmpty()) return;

            boolean hasUnclaimable = false;

            Set<String> claimedRowId = new HashSet<>();

            for (String rewardData : rewards) {
                String[] details = rewardData.split(";");
                String rewardId = details[1];

                if(!eventRewards.containsKey(rewardId)) {
                    hasUnclaimable = true;
                    continue;
                }

                List<String> commands = eventRewards.get(rewardId).commands();

                for(String cmd : commands){
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",player.getName()));
                }

                claimedRowId.add(details[0]);
                LoggerUtils.file("EventRewards",player.getName() + " claimed rewards: " + rewardData);
            }

            if(hasUnclaimable){
                SoundUtils.playSound(player,"block.chest.locked");
                player.sendMessage(MessageUtils.process("<aqua>[<#FFA500>Event Rewards<aqua>] 1 or more rewards are not set up correctly, please report to a developer.",false));
            } else {
                SoundUtils.playSound(player,"block.chest.open");
                player.sendMessage(MessageUtils.process("<aqua>[<#FFA500>Event Rewards<aqua>] You have claimed all the rewards.",false));
            }

            if(!claimedRowId.isEmpty())
                MySQLManager.getInstance().updateAllClaimedEventRewards(claimedRowId);
        });
    }

    public void updateInventory(String uuid, int newPage, Inventory inv){

        List<String> allRewards = cachedPlayerRewards.get(uuid);

        // Dont do anything if empty
        if(allRewards == null) return;

        int totalPages = (int) Math.ceil((double) allRewards.size() / 28);
        // Skip if last or first page
        if(newPage > totalPages || newPage < 1){
            return;
        }
        // Update Page Number
        ItemStack claimAllButton = inv.getItem(49);
        ItemMeta claimAllButtonMeta = claimAllButton.getItemMeta();
        claimAllButtonMeta.getPersistentDataContainer().set(pageNSK, PersistentDataType.INTEGER, newPage);
        claimAllButton.setItemMeta(claimAllButtonMeta);

        // Prepare new page
        int pageSize = 28;
        int start = (newPage - 1) * pageSize;
        int end = Math.min(start + pageSize, allRewards.size());

        List<String> pageRewards = allRewards.subList(start, end);

        // Clear the page
        for(int i = 10; i <= 43; i++){
            if(i % 9 == 0 || i % 9 == 8) continue;

            inv.setItem(i, null);
        }

        // Repopulate the page
        int rewardSlot = 10;
        for (String rewardDetails : pageRewards) {
            // Make sure only add into reward slot
            while (rewardSlot < 44 && InventoryUtils.isBorderSlot(rewardSlot)) {
                rewardSlot++;
            }

            // Stop adding extra reward
            if (rewardSlot >= 44) break;

            ItemStack rewardItem = createRewardItem(rewardDetails);
            inv.setItem(rewardSlot, rewardItem);
            rewardSlot++;
        }
    }

    public void checkUnclaimedReward(Player player){
        MySQLManager.getInstance().getAllEventRewards(String.valueOf(player.getUniqueId()), serverName, rewards -> {
            if(!rewards.isEmpty()){
                SoundUtils.playSound(player,"block.amethyst_block.resonate");
                player.sendMessage(MessageUtils.process(
                        "<aqua>[<gold>Event Rewards<aqua>] <gold>You have <red>"
                                + rewards.size()
                                + " <gold>unclaimed reward(s). Claim them with <red>/eventrewards claim<gold>!"
                ));
            }
        });
    }

    public boolean isRewardIdValid(String rewardId){
        return eventRewards.containsKey(rewardId);
    }

    public void clearPlayerRewardsCache(String uuid){
        cachedPlayerRewards.remove(uuid);
    }

    private ItemStack createRewardItem(String rewardDetails){
        String[] details = rewardDetails.split(";");
        String rowId = details[0];
        String rewardId = details[1];
        String timeStamp = details[2];

        ItemStack rewardHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) rewardHead.getItemMeta();

        if(skullMeta != null){
            skullMeta.displayName(MessageUtils.process(getRewardDisplayName(rewardId)));
            String base64 = getRewardSkullTexture(rewardId);

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add((new ProfileProperty("textures", base64)));
            skullMeta.setPlayerProfile(profile);

            List<String> loreList = new ArrayList<>(getRewardLoreList(rewardId));
            loreList.add("<blue>Time Received: <green>" + timeStamp + " GMT+8");

            List<Component> deserializedLoreList = loreList.stream().map(MessageUtils::process).toList();

            skullMeta.lore(deserializedLoreList);

            PersistentDataContainer skullPDC = skullMeta.getPersistentDataContainer();
            skullPDC.set(new NamespacedKey("holoutils","reward_id"), PersistentDataType.STRING, rewardId);
            skullPDC.set(new NamespacedKey("holoutils","row_id"), PersistentDataType.STRING, rowId);

            rewardHead.setItemMeta(skullMeta);
        }

        return rewardHead;
    }

    private String getRewardDisplayName(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        return (reward != null && reward.displayName() != null)
            ? reward.displayName() : "<gold>" + rewardId;
    }

    private List<String> getRewardLoreList(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        return (reward != null && reward.displayName() != null)
            ? reward.loreLines() : List.of("");
    }

    private String getRewardSkullTexture(String rewardId){
        EventReward reward = eventRewards.get(rewardId);

        return (reward != null && reward.skullTexture() != null)
            ? reward.skullTexture()
            : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIyZDRiZTFhYmNmMzgzMmM5MTYxOTFkMjRmOTYwN2JmMTk0ZWZmOGRmYmYzYjk1MjBiZDk3MjQwZTdjOCJ9fX0=";
    }
}
