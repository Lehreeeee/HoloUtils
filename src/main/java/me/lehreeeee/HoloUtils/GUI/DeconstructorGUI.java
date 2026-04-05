package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.utils.InventoryUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeconstructorGUI implements InventoryHolder {

    private final Inventory inventory;

    public DeconstructorGUI(){
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.process("<gold>Deconstructor"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        prepareInventory();
        return this.inventory;
    }

    public List<ItemStack> getReturnItems(){
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < 54; i++) {
            if (InventoryUtils.isBorderSlot(i)) continue;

            items.add(this.inventory.getItem(i));
        }

        return items;
    }

    public int getEmptySlot(){
        for (int i = 0; i < 54; i++) {
            if (InventoryUtils.isBorderSlot(i)) continue;
            if (inventory.getItem(i) == null) return i;
        }
        return -1;
    }

    private void prepareInventory(){
        // Glass Pane to fill the border
        ItemStack fillGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillGlassPaneMeta = fillGlassPane.getItemMeta();
        if (fillGlassPaneMeta != null){
            fillGlassPaneMeta.displayName(MessageUtils.process("<#FFA500>I'm orange glass pane."));
            fillGlassPane.setItemMeta(fillGlassPaneMeta);
        }

        // Deconstruct button
        ItemStack deconstructButton = new ItemStack(Material.MACE);
        ItemMeta deconstructButtonMeta = deconstructButton.getItemMeta();
        if (deconstructButtonMeta != null){
            deconstructButtonMeta.displayName(MessageUtils.process("<red><b>Deconstruct All"));
            deconstructButton.setItemMeta(deconstructButtonMeta);
        }

        // Load Items button
        ItemStack loadItemsButton = new ItemStack(Material.NETHER_STAR);
        ItemMeta loadItemsButtonMeta = loadItemsButton.getItemMeta();
        if (loadItemsButtonMeta != null){
            loadItemsButtonMeta.displayName(MessageUtils.process("<aqua><b>Load Items"));
            loadItemsButtonMeta.lore(List.of(
                    MessageUtils.process("<gray>Click to load deconstructable"),
                    MessageUtils.process("<gray>items from your inventory.")
            ));

            loadItemsButton.setItemMeta(loadItemsButtonMeta);
        }

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (InventoryUtils.isBorderSlot(i)) {
                inventory.setItem(i, fillGlassPane);
            }
        }

        inventory.setItem(49, deconstructButton);
        inventory.setItem(53, loadItemsButton);
    }
}
