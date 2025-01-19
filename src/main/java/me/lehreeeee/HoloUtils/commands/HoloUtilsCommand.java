
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.StatusDisplayManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
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
    private final TitleDisplayManager titleDisplayManager;

    public HoloUtilsCommand(HoloUtils plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.titleDisplayManager = TitleDisplayManager.getInstance();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        // Development mode, only admin can use
        if(!sender.hasPermission("holoutils.admin")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("playertitle") && sender instanceof Player player){
                if(titleDisplayManager.getAvailableTitles(player).isEmpty()) {
                    sendFeedbackMessage(sender, "<#FFA500>You don't have any player title.");
                    return true;
                }

                player.openInventory(new PlayerTitleGUI().createPlayerTitleGUI(player));
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

        if(args[0].equalsIgnoreCase("statusdisplay") && args.length == 4){
            try{
                StatusDisplayManager.getInstance().setStatusDisplay(UUID.fromString(args[1]),args[2],Long.valueOf(args[3]));
            } catch(NumberFormatException e){
                sendFeedbackMessage(sender,"Invalid duration, expected Long but get " + args[3]);
            } catch (IllegalArgumentException e){
                sendFeedbackMessage(sender,"Invalid UUID - " + args[1]);
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("pdc") && sender instanceof Player player){
            ItemPDCEditor pdcEditor = new ItemPDCEditor(plugin,player.getInventory().getItemInMainHand());

            // /holoutils pdc get advancedenchantments:mobtrak
            if(args[1].equalsIgnoreCase("get") && args.length == 3){
                String result = pdcEditor.getData(args[2]);
                if(result != null) {
                    sendFeedbackMessage(player, "<#FFA500>Value of <aqua>"+ args[2] + " <#FFA500>is<aqua> " + result);
                } else {
                    sendFeedbackMessage(player,"Something went wrong. Is the key correct?");
                }
                return true;
            }

            // /holoutils pdc remove advancedenchantments:mobtrak
            if(args[1].equalsIgnoreCase("remove") && args.length == 3){
                if(pdcEditor.removeData(args[2])){
                    // Update the item
                    player.getInventory().setItemInMainHand(pdcEditor.getItemStack());
                    sendFeedbackMessage(player, "<#FFA500>Value of <aqua>"+ args[2] + " <#FFA500>has been removed.");
                } else {
                    sendFeedbackMessage(player,"Something went wrong. Is the key correct?");
                }
                return true;
            }

            // /holoutils pdc set [namespace] [key] [DataType] [Value]
            // /holoutils pdc set advancedenchantments mobtrak STRING 0
            if(args[1].equalsIgnoreCase("set") && args.length == 6){
                if(pdcEditor.setData(args[2], args[3], args[4], args[5])){
                    // Update the item
                    player.getInventory().setItemInMainHand(pdcEditor.getItemStack());
                    sendFeedbackMessage(player, "<#FFA500>Set key <aqua>" + args[2] + ":" + args[3] + " <#FFA500>to value<aqua> " + args[5]);
                } else {
                    sendFeedbackMessage(player,"Something went wrong. Figure it out yourself :suiwheeze:");
                }
                return true;
            }
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
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils reload <white>-<aqua> Take a guess.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils playertag <white>-<aqua> Open gui to select display tag, must have atleast 1 tag permission to use.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils pdc get [namespacedkey] <white>-<aqua> Get the value of the namespaced key in mainhand item's pdc.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils pdc remove [namespacedkey] <white>-<aqua> Remove the data from mainhand item's pdc.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/HoloUtils pdc set [namespace] [key] [datatype] [value] <white>-<aqua> Add data to mainhand item's pdc.",false));
        }
        else{
            logger.info("Command Usage:");
            logger.info("/HoloUtils help - Show command usage.");
            logger.info("/HoloUtils reload - Take a guess.");
            logger.info("/HoloUtils playertag - Open gui to select display tag, must have atleast 1 tag permission to use.");
            logger.info("/HoloUtils pdc get [namespacedkey] - Get the value of the namespaced key in mainhand item's pdc.");
            logger.info("/HoloUtils pdc remove [namespacedkey] - Remove the data from mainhand item's pdc.");
            logger.info("/HoloUtils pdc set [namespace] [key] [datatype] [value] - Add data to mainhand item's pdc.");
        }
    }
}

