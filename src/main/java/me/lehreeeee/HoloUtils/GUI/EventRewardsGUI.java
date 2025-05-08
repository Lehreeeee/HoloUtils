package me.lehreeeee.HoloUtils.GUI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.managers.EventRewardsManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class EventRewardsGUI implements InventoryHolder {
    private final Inventory inventory;

    public EventRewardsGUI(){
        this.inventory = Bukkit.createInventory(this, 54, MessageHelper.process("<gold>Event Rewards"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public Inventory getEventRewardsInventory(String uuid){
        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageHelper.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Claim all button
        ItemStack claimAllButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta updateButtonMeta = claimAllButton.getItemMeta();
        if (updateButtonMeta != null){
            updateButtonMeta.displayName(MessageHelper.process("<green><b>Claim All"));
            claimAllButton.setItemMeta(updateButtonMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (isBorderSlot(i)) {
                inventory.setItem(i, fillGlassPane);
            }
        }

        // Set claim all button
        inventory.setItem(49, claimAllButton);

        // Insert rewards after data returned via callback
        EventRewardsManager.getInstance().getRewards(uuid, rewards -> {
            int rewardSlot = 9;
            for (String rewardDetails : rewards) {
                // Make sure only add into reward slot and stop
                while (isBorderSlot(rewardSlot)) {
                    rewardSlot++;
                }

                // TODO: Add more pages for more than 28 rewards
                if (rewardSlot >= 44) break;

                ItemStack rewardItem = createRewardItem(rewardDetails);
                inventory.setItem(rewardSlot, rewardItem);
                rewardSlot++;
            }
        });

        return inventory;
    }

    private boolean isBorderSlot(int slot){
        return slot <= 8 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
    }

    private ItemStack createRewardItem(String rewardDetails){
        String[] details = rewardDetails.split(";");
        String rewardId = details[0];
        String timeStamp = details[1];
        String rowId = details[2];

        ItemStack rewardHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) rewardHead.getItemMeta();

        if(skullMeta != null){
            skullMeta.displayName(MessageHelper.process(EventRewardsManager.getInstance().getRewardDisplayName(rewardId)));
            String base64 = EventRewardsManager.getInstance().getRewardSkullTexture(rewardId);

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
}
