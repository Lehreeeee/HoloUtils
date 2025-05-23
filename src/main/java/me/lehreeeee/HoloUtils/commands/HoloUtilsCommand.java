
package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.GUI.PlayerTitleGUI;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.managers.DamageLeaderboardManager;
import me.lehreeeee.HoloUtils.managers.RedisManager;
import me.lehreeeee.HoloUtils.managers.StatusDisplayManager;
import me.lehreeeee.HoloUtils.managers.TitleDisplayManager;
import me.lehreeeee.HoloUtils.utils.ItemPDCEditor;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HoloUtilsCommand implements CommandExecutor {
    private final HoloUtils plugin;

    public HoloUtilsCommand(HoloUtils plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        // Only admin can use
        if(!sender.hasPermission("holoutils.admin")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
            return true;
        }

        if(args.length == 0){
            sendCommandUsage(sender);
            return true;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("playertitle") && sender instanceof Player player){
                if(TitleDisplayManager.getInstance().getAvailableTitles(player).isEmpty()) {
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

        if(args[0].equalsIgnoreCase("damagelb")){
            if (args.length == 3) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(args[2]);
                } catch (IllegalArgumentException e) {
                    sendFeedbackMessage(sender, "Invalid UUID: " + args[2]);
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "track":
                        if(!DamageLeaderboardManager.getInstance().trackEntity(uuid)){
                            sendFeedbackMessage(sender,"Can't track entity " + uuid + ", it's already in tracked list or has a parent.");
                        } else {
                            sendFeedbackMessage(sender,"Started tracking entity " + uuid + ".");
                        }
                        return true;
                    case "untrack":
                        if(!DamageLeaderboardManager.getInstance().untrackEntity(uuid)){
                            sendFeedbackMessage(sender,"Can't untrack entity " + uuid + ", it's not in tracked list.");
                        } else {
                            sendFeedbackMessage(sender,"Stopped tracking entity " + uuid + ".");
                        }
                        return true;
                    case "unlink":
                        UUID parentUUID = DamageLeaderboardManager.getInstance().unLinkEntity(uuid);
                        if(parentUUID == null){
                            sendFeedbackMessage(sender,"Can't unlink entity " + uuid + ", it has no parent.");
                        } else {
                            sendFeedbackMessage(sender,"Unlinked entity " + uuid + " from it's parent " + parentUUID + ".");
                        }
                        return true;
                    case "reset":
                        if(!DamageLeaderboardManager.getInstance().resetLeaderboard(uuid)){
                            sendFeedbackMessage(sender,"Can't reset leaderboard for entity " + uuid + ", it has no leaderboard or already ended.");
                        } else {
                            sendFeedbackMessage(sender,"Successfully reset leaderboard for entity" + uuid + ".");
                        }
                        return true;
                    default:
                        sendFeedbackMessage(sender,"<#FFA500>Unknown command. Check /HoloUtils help.");
                        return true;
                }
            }

            if(args.length == 4 && args[1].equalsIgnoreCase("link")){
                try{
                    UUID childUUID = UUID.fromString(args[2]);
                    UUID parentUUID = UUID.fromString(args[3]);

                    if(DamageLeaderboardManager.getInstance().linkEntity(childUUID,parentUUID)){
                        sendFeedbackMessage(sender,"Child entity " + childUUID + " has parent previously, replacing with " + parentUUID + ".");
                    } else {
                        sendFeedbackMessage(sender,"Linked child entity " + childUUID + " to it's parent " + parentUUID + ".");
                    }
                    return true;
                } catch (IllegalArgumentException e){
                    sendFeedbackMessage(sender,"Invallid UUID: '" + args[2] + "' or '" + args[3] + "'");
                }
            }

            if(args[1].equalsIgnoreCase("list")  && args.length == 2){
                sendTrackAndLinkedEntityList(sender);
                return true;
            }

            return true;
        }

        if(args[0].equalsIgnoreCase("statuseffect") && args.length == 4){
            try{
                StatusDisplayManager.getInstance().setStatusDisplay(UUID.fromString(args[1]),args[2],Long.valueOf(args[3]));
            } catch(NumberFormatException e){
                sendFeedbackMessage(sender,"Invalid duration, expected Long but get " + args[3]);
            } catch (IllegalArgumentException e){
                sendFeedbackMessage(sender,"Invalid UUID: '" + args[1] + "'");
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

        if(args[0].equalsIgnoreCase("testredis") && args.length == 3){
            RedisManager.getInstance().publish(args[1],args[2]);
            return true;
        }

        sendFeedbackMessage(sender,"<#FFA500>Unknown command. Check /HoloUtils help.");
        return true;
    }

    private void sendTrackAndLinkedEntityList(CommandSender sender){
        boolean hasTrackedEntity = false;
        boolean hasLinkedEntity = false;

        Set<UUID> trackedEntities = DamageLeaderboardManager.getInstance().getTrackedEntities();
        Set<Map.Entry<UUID,UUID>> linkedEntities = DamageLeaderboardManager.getInstance().getLinkedEntities();

        StringBuilder message = new StringBuilder("Tracked entities:\n");

        if(!trackedEntities.isEmpty()){
            hasTrackedEntity = true;
            for(UUID uuid : trackedEntities){
                message.append("<green>").append(uuid).append("<white>, ");
            }
            if(!message.isEmpty()){
                message.setLength(message.length() - 2);
            }
        }

        if(!linkedEntities.isEmpty()){
            hasLinkedEntity = true;
            message.append("\nChildren(Parent):\n");
            for(Map.Entry<UUID,UUID> entry : linkedEntities){
                message.append(MessageFormat.format("<green>{0}<white>(<blue>{1}<white>)<white>, ",entry.getKey(),entry.getValue()));
            }
            if(!message.isEmpty()){
                message.setLength(message.length() - 2);
            }
        }

        if(hasTrackedEntity || hasLinkedEntity)
            sendFeedbackMessage(sender,message.toString());
        else
            sendFeedbackMessage(sender,"There is no tracked or linked entity.");
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtils.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }

    private void sendCommandUsage(CommandSender sender){
        if (sender instanceof Player) {
            // Dont send if not admin
            if(!sender.hasPermission("holoutils.admin")) {
                sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
                return;
            }

            sender.sendMessage(MessageHelper.process("<#FFA500>Command Usage:",true));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils help <white>-<aqua> Show command usage.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils reload <white>-<aqua> Take a guess.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils playertitle <white>-<aqua> Open gui to select display tag, must have atleast 1 tag permission to use.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils statuseffect [uuid] [effect] [ticks] <white>-<aqua> Apply status effect display on the mob.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils pdc get [namespacedkey] <white>-<aqua> Get the value of the namespaced key in mainhand item's pdc.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils pdc remove [namespacedkey] <white>-<aqua> Remove the data from mainhand item's pdc.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils pdc set [namespace] [key] [datatype] [value] <white>-<aqua> Add data to mainhand item's pdc.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/holoutils testredis <white>-<aqua> For debug purpose, don't touch if you don't know what it does.",false));
        }
        else{
            LoggerUtils.info("Command Usage:");
            LoggerUtils.info("/holoutils help - Show command usage.");
            LoggerUtils.info("/holoutils reload - Take a guess.");
            LoggerUtils.info("/holoutils playertitle - Open gui to select display tag, must have atleast 1 tag permission to use.");
            LoggerUtils.info("/holoutils statuseffect [uuid] [effect] [ticks] - Apply status effect display on the mob.");
            LoggerUtils.info("/holoutils pdc get [namespacedkey] - Get the value of the namespaced key in mainhand item's pdc.");
            LoggerUtils.info("/holoutils pdc remove [namespacedkey] - Remove the data from mainhand item's pdc.");
            LoggerUtils.info("/holoutils pdc set [namespace] [key] [datatype] [value] - Add data to mainhand item's pdc.");
            LoggerUtils.info("/holoutils testredis - For debug purpose, don't touch if you don't know what it does.");
        }
    }
}

