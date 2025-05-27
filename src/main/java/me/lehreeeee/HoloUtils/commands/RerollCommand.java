
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.RerollGUI;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RerollCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("holoutils.reroll")){
            MessageUtils.sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
            return true;
        }

        if(args.length == 0 && sender instanceof Player player){
            player.openInventory(new RerollGUI().getInventory());
            return true;
        }

        // Ignore the rest
        MessageUtils.sendFeedbackMessage(sender,"<#FFA500>Correct usage: /reroll");
        return true;
    }
}

