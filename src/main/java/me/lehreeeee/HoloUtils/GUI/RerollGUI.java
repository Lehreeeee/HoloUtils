package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class RerollGUI implements InventoryHolder {

    private final Inventory inventory;

    public RerollGUI(){
        this.inventory = Bukkit.createInventory(this, 27, MessageHelper.process("<gold>Reroll Inventory"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
