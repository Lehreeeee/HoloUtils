
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.managers.MySQLManager;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class ClaimAccessoriesCommand implements CommandExecutor {
    private final Logger logger;


    public ClaimAccessoriesCommand(Logger logger) {
        this.logger = logger;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("holoutils.claimaccessories")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
            return true;
        }

        /*
        TODO: Temporary command for accessories rework. Remove after 3 months along with mmoinventory_inventories_rework table.
        */
        if(args.length == 0 && sender instanceof Player player){
            MySQLManager.getInstance().claimOldAccessories(player.getUniqueId().toString());
            return true;
        }

        // Ignore the rest
        sendFeedbackMessage(sender,"<#FFA500>Correct usage: /claimaccessories");
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        logger.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }
}

