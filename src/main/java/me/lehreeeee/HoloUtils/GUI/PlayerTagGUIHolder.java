package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PlayerTagGUIHolder implements InventoryHolder {

    private final Inventory inventory;

    public PlayerTagGUIHolder() {
        this.inventory = Bukkit.createInventory(this, 54, MessageHelper.process(TitleDisplayManager.getInstance().getGUIName()));
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
