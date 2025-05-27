
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.managers.MySQLManager;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimAccessoriesCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("holoutils.claimaccessories")){
            MessageUtils.sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
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
        MessageUtils.sendFeedbackMessage(sender,"<#FFA500>Correct usage: /claimaccessories");
        return true;
    }
}

