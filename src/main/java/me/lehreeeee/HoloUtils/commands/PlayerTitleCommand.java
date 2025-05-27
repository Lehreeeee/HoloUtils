
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUI;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
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
                sendFeedbackMessage(sender, "<#FFA500>You don't have any player title.");
                return true;
            }

            player.openInventory(new PlayerTitleGUI().createPlayerTitleGUI(player));
            return true;
        }

        // Ignore the rest
        sendFeedbackMessage(sender,"<#FFA500>Correct usage: /playertitle or /ptitle");
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtils.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }
}

