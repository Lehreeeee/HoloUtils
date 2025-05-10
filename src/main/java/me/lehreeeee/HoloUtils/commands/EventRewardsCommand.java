
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.EventRewardsGUI;
import me.lehreeeee.HoloUtils.managers.EventRewardsManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventRewardsCommand implements CommandExecutor {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){

        if(args.length == 1 && args[0].equalsIgnoreCase("claim") && sender instanceof Player player){
            if (!player.hasPermission("holoutils.eventrewards.claim")) {
                sendFeedbackMessage(sender,"You don't have permission to use this command.");
                return true;
            }

            player.openInventory(new EventRewardsGUI().getEventRewardsInventory(String.valueOf(player.getUniqueId())));
            return true;
        }

        // Only admin can continue after this point
        if (!sender.hasPermission("holoutils.eventrewards.admin")) {
            sendFeedbackMessage(sender,"You don't have permission to use this command.");
            return true;
        }

        if(args.length == 4 && args[0].equalsIgnoreCase("give")){
            EventRewardsManager.getInstance().giveRewards(String.valueOf(Bukkit.getPlayerUniqueId(args[1])),args[2],args[3]);
            sendFeedbackMessage(sender,"Gave event reward '" + args[2]+ "' to " + args[1] + " for server(s) " + args[3] + ".");
            return true;
        }

        sendCommandUsage(sender);
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtils.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }

    private void sendCommandUsage(CommandSender sender){
        if (sender instanceof Player) {
            sender.sendMessage(MessageHelper.process("<#FFA500>Command Usage:",true));
            sender.sendMessage(MessageHelper.process("<#FFA500>/eventrewards help <white>-<aqua> Show command usage.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/eventrewards claim <white>-<aqua> Open GUI to claim rewards.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/eventrewards give [player] [rewardId] [server] <white>-<aqua> Give rewards to player.",false));
        }
        else{
            LoggerUtils.info("Command Usage:");
            LoggerUtils.info("<#FFA500>/eventrewards help <white>-<aqua> Show command usage.");
            LoggerUtils.info("<#FFA500>/eventrewards claim <white>-<aqua> Open GUI to claim rewards.");
            LoggerUtils.info("<#FFA500>/eventrewards give [player] [rewardId] [server] <white>-<aqua> Give rewards to player.");
        }
    }
}

