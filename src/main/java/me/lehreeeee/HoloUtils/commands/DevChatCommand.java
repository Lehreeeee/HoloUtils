
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.managers.DevChatManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtil;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DevChatCommand implements CommandExecutor {

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("holoutils.devchat")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
            return true;
        }

        // /devchat
        if(args.length == 0){
            DevChatManager devChatManager = DevChatManager.getInstance();
            // Same as command to toggle on/off, just simpler
            if(sender instanceof Player player){
                boolean toggledON = devChatManager.toggleDevChat(player.getUniqueId(), !devChatManager.hasDevChatOn(player.getUniqueId()));

                if(toggledON){
                    sendFeedbackMessage(sender,"<#FFA500>Toggled <green>on <#FFA500>dev chat.");
                } else{
                    sendFeedbackMessage(sender,"<#FFA500>Toggled <red>off <#FFA500>dev chat.");
                }

                return true;
            }

            sendFeedbackMessage(sender,"Console cannot toggle dev chat, just use /devchat [message] :amewtf:");
            return true;
        }

        /*
        /devchat [on/off]
        /devchat [help]
        /devchat [message]
        */
        if(args.length == 1){
            if(sender instanceof Player player){
                DevChatManager devChatManager = DevChatManager.getInstance();
                // Toggle on if not already toggled on
                if(args[0].equalsIgnoreCase("on") && !devChatManager.hasDevChatOn(player.getUniqueId())){
                    devChatManager.toggleDevChat(player.getUniqueId(),true);
                    sendFeedbackMessage(sender,"<#FFA500>Toggled <green>on <#FFA500>dev chat.");
                    return true;
                }
                // Toggle off if already toggled on
                if(args[0].equalsIgnoreCase("off") && devChatManager.hasDevChatOn(player.getUniqueId())){
                    devChatManager.toggleDevChat(player.getUniqueId(),false);
                    sendFeedbackMessage(sender,"<#FFA500>Toggled <red>off <#FFA500>dev chat.");
                    return true;
                }
            }

            if(args[0].equalsIgnoreCase("help")){
                sendCommandUsage(sender);
                return true;
            }
        }

        // If none the above, send it as message
        DevChatManager.getInstance().publishMessage(sender, String.join(" ", args));
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtil.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }

    private void sendCommandUsage(CommandSender sender){
        if (sender instanceof Player) {
            sender.sendMessage(MessageHelper.process("<#FFA500>Command Usage:",true));
            sender.sendMessage(MessageHelper.process("<#FFA500>What u need help for, never use staffchat b4 is it :amewtf:",false));
        }
        else{
            LoggerUtil.info("Command Usage:");
            LoggerUtil.info("<#FFA500>What u need help for, never use staffchat b4 is it :amewtf:");
        }
    }
}

