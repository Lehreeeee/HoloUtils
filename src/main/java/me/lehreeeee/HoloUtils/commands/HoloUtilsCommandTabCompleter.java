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
    private final List<String> commands = List.of("reload", "help", "playertag", "elementstatus","pdc");
    private final List<String> supportedDataTypes = List.of("STRING","INTEGER","FLOAT","DOUBLE","LONG");

    public HoloUtilsCommandTabCompleter(HoloUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 1){
            return commands;
        }

        if(args[0].equalsIgnoreCase("pdc") && sender instanceof Player player){
            if(args.length == 2) return List.of("get","set","remove");

            if(args.length == 3 && List.of("get","set","remove").contains(args[1])){
                ItemPDCEditor pdcEditor = new ItemPDCEditor(plugin,player.getInventory().getItemInMainHand());
                return pdcEditor.getNSKs();
            }
        }

        return null;
    }
}
