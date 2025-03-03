package me.lehreeeee.HoloUtils.GUI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lehreeeee.HoloUtils.managers.RerollManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RerollGUI implements InventoryHolder {

    private final Inventory inventory;

    public RerollGUI(){
        this.inventory = Bukkit.createInventory(this, 27, MessageHelper.process("<gold>Reroll"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        prepareInventory();
        return this.inventory;
    }

    private void prepareInventory(){
        // Add fill items
        ItemStack fill = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta itemMeta = fill.getItemMeta();
        itemMeta.setCustomModelData(15);
        itemMeta.displayName(null);
        itemMeta.setHideTooltip(true);
        fill.setItemMeta(itemMeta);

        for(int i = 0; i < 27; i++){
            if(i != 11 && i != 15) { inventory.setItem(i,fill); }
        }

        // Add reroll button
        ItemStack diceHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) diceHead.getItemMeta();

        if(skullMeta != null){
            skullMeta.displayName(MessageHelper.process("<white>\uD83C\uDFB2 <gold>Reroll <white>\uD83C\uDFB2"));
            String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFiZDljOGNmZjQ1ZThjZDdjNjdiMzBhNzc5YjQwNWNmOWMyYzRlY2U5ZDEzMTRmOTdmY2EwYjRmZmM4YzFjNSJ9fX0=";

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add((new ProfileProperty("textures", base64)));
            skullMeta.setPlayerProfile(profile);

            skullMeta.lore(RerollManager.getInstance().getDefaultDiceLore());

            diceHead.setItemMeta(skullMeta);
        }

        inventory.setItem(15,diceHead);

        // Add glass pane to reroll slot
        ItemStack glassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta glassPaneMeta = glassPane.getItemMeta();
        glassPaneMeta.displayName(MessageHelper.process("<aqua>Put the item you wish to reroll here."));

        glassPane.setItemMeta(glassPaneMeta);

        inventory.setItem(11,glassPane);
    }
}
