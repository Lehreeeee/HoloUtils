
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUI;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerTitleCommand implements CommandExecutor {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        // Player Section
        if(args.length == 0 && sender instanceof Player player) {
            if(TitleDisplayManager.getInstance().getAvailableTitles(player).isEmpty()) {
                MessageUtils.sendFeedbackMessage(sender, "<#FFA500>You don't have any player title.");
                return true;
            }

            player.openInventory(new PlayerTitleGUI(player).getPlayerTitleGUI());
            return true;
        }

        // Ignore the rest
        MessageUtils.sendFeedbackMessage(sender,"<#FFA500>Correct usage: /playertitle or /ptitle");
        return true;
    }
}

