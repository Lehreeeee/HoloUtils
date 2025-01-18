
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.PlayerTagGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.TagDisplayManager;
import me.lehreeeee.HoloUtils.utils.ItemPDCEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.lehreeeee.HoloUtils.utils.MessageHelper;

import java.util.UUID;
import java.util.logging.Logger;

public class HoloUtilsCommand implements CommandExecutor {
    private final HoloUtils plugin;
    private final Logger logger;
    private final TagDisplayManager tagDisplayManager;

    public HoloUtilsCommand(HoloUtils plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.tagDisplayManager = TagDisplayManager.getInstance();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        // Development mode, only admin can use
        if(!sender.hasPermission("holoutils.admin")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("playertag") && sender instanceof Player player){
                if(TagDisplayManager.getInstance().getAvailableTags(player).isEmpty()) {
                    sendFeedbackMessage(sender, "<#FFA500>You don't have any player tag.");
                    return true;
                }

                player.openInventory(new PlayerTagGUI().createPlayerTagGUI(player));
                return true;
            }

            if(args[0].equalsIgnoreCase("reload")){
                sendFeedbackMessage(sender,"<#FFA500>Reloading HoloUtils...");

                // Reload config here
                plugin.reloadConfig();
                // Reload data from the new config
                plugin.reloadData();

                sendFeedbackMessage(sender,"<#FFA500>Successfully reloaded HoloUtils.");
                return true;
            }

            if(args[0].equalsIgnoreCase("help")){
                sendCommandUsage(sender);
                return true;
            }
        }

        // /holoutils pdc [get/set] [namespaced key] [datatype] [value]
        if(args[0].equalsIgnoreCase("pdc") && sender instanceof Player player){
            ItemPDCEditor pdcEditor = new ItemPDCEditor(plugin,player.getInventory().getItemInMainHand());

            if(args[1].equalsIgnoreCase("get") && args.length == 3){
                String result = pdcEditor.getData(args[2]);
                if(result != null) sendFeedbackMessage(player, "<#FFA500>Value of <aqua>"+ args[2] + " <#FFA500>is<aqua> " + result);
                else sendFeedbackMessage(player,"Something went wrong. Is the key correct?");
                return true;
            }
            return true;
        }

        sendFeedbackMessage(sender,"<#FFA500>Unknown command. Check /HoloUtils help.");
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        logger.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }

    private void sendCommandUsage(CommandSender sender){
        if (sender instanceof Player) {
            sender.sendMessage(MessageHelper.process("<#FFA500>Command Usage:",true));
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils help <white>-<aqua> Show command usage.",false));
        }
        else{
            logger.info("Command Usage:");
            logger.info("/HoloUtils help - Show command usage.");
        }
    }
}

