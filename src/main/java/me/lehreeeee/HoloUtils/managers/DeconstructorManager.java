package me.lehreeeee.HoloUtils.managers;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import me.lehreeeee.HoloUtils.utils.SoundUtils;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DeconstructorManager {
    private static DeconstructorManager instance;
    private final List<String> deconstructableItems = new ArrayList<>();

    public static DeconstructorManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DeconstructorManager not initialized.");
        }
        return instance;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new DeconstructorManager();
        }
    }

    public void loadDeconstructorConfig(ConfigurationSection deconstructorConfig){
        deconstructableItems.clear();

        if(deconstructorConfig == null) {
            LoggerUtils.warning("Unable to find deconstructor config section. No deconstruction can be done.");
            return;
        }

        deconstructableItems.addAll(deconstructorConfig.getStringList("allowed-tiers"));
    }

    public void deconstructItems(Player player, Inventory clickedInv){
        SoundUtils.playSound(player,"block.anvil.destroy");

        List<ItemStack> finalLoot = new ArrayList<>();
        int itemCount = 0;
        int failedCount = 0;

        for (int i = 0; i < 54; i++) {
            ItemStack item = clickedInv.getItem(i);
            if (InventoryUtils.isBorderSlot(i) || item == null) continue;

            NBTItem nbtItem = NBTItem.get(item);

            String tag = nbtItem.getString("MMOITEMS_TIER");
            if(tag.isBlank() || !isDeconstructable(tag)) continue;

            ItemTier tier = MMOItems.plugin.getTiers().get(tag);
            if(tier == null) continue;

            PlayerData data = PlayerData.get(player);
            List<ItemStack> loot = tier.getDeconstructedLoot(data);

            item.setAmount(item.getAmount() - 1);

            itemCount++;
            if(loot.isEmpty()) failedCount++;

            finalLoot.addAll(loot);
        }

        InventoryUtils.giveItem(finalLoot,player);
        player.sendMessage(MessageUtils.process(itemCount > 0
                        ? MessageFormat.format("Deconstructed {0} item(s). Success: {1} Failed: {2}",itemCount,itemCount-failedCount,failedCount)
                        : "No deconstructable items.",
                true,
                "Deconstructor"));
    }

    public boolean isDeconstructable(String tier){
        return deconstructableItems.contains(tier);
    }
}
