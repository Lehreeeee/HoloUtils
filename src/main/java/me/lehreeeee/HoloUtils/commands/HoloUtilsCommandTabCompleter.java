package me.lehreeeee.HoloUtils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoloUtilsCommandTabCompleter implements TabCompleter {
    private final List<String> commands = List.of("reload", "help");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 1){
            return commands;
        }

        return null;
    }
}
