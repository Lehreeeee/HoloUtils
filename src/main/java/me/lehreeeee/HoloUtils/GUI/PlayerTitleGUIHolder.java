package me.lehreeeee.HoloUtils.GUI;

import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class PlayerTitleGUIHolder implements InventoryHolder {

    private final Inventory inventory;

    public PlayerTitleGUIHolder() {
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.process(TitleDisplayManager.getInstance().getGUIName()));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
