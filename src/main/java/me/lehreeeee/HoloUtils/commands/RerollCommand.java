
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.RerollGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class RerollCommand implements CommandExecutor {
    private final Logger logger;
    private final HoloUtils plugin;

    public RerollCommand(HoloUtils plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("holoutils.reroll")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
            return true;
        }

        if(args.length == 0 && sender instanceof Player player){
            player.openInventory(new RerollGUI().getInventory());
            return true;
        }

        // Ignore the rest
        sendFeedbackMessage(sender,"<#FFA500>Correct usage: /reroll");
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        logger.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }
}

