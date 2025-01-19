package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.ItemPDCEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoloUtilsCommandTabCompleter implements TabCompleter {
    private final HoloUtils plugin;
    private final List<String> commands = List.of("reload", "help", "playertitle", "elementstatus","pdc");
    private final List<String> pdcActions = List.of("get","set","remove");
    private final List<String> typeNames = List.of(
            "STRING",
            "INTEGER",
            "FLOAT",
            "DOUBLE",
            "LONG",
            "BYTE"
    );


    public HoloUtilsCommandTabCompleter(HoloUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 1){
            return commands;
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("pdc")){
                return pdcActions;
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("pdc") && sender instanceof Player player){
                if(List.of("get","remove").contains(args[1])){
                    ItemPDCEditor pdcEditor = new ItemPDCEditor(plugin,player.getInventory().getItemInMainHand());
                    return pdcEditor.getNSKs();
                } else if(args[1].equalsIgnoreCase("set")){
                    return List.of("Namespace");
                }
            }
        }

        if(args.length == 4) {
            if(args[0].equalsIgnoreCase("pdc") && args[1].equalsIgnoreCase("set")) {
                return List.of("Key");
            }
        }

        if(args.length == 5){
            if(args[0].equalsIgnoreCase("pdc") && args[1].equalsIgnoreCase("set")){
                return typeNames;
            }
        }

        if(args.length == 6){
            if(args[0].equalsIgnoreCase("pdc") && args[1].equalsIgnoreCase("set")){
                return List.of("Value");
            }
        }

        return null;
    }
}
