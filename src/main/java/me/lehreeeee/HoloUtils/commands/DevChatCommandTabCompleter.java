package me.lehreeeee.HoloUtils.commands;

import me.lehreeeee.HoloUtils.HoloUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DevChatCommandTabCompleter implements TabCompleter {
    private final HoloUtils plugin;
    private final List<String> commands = List.of("on", "off", "help");


    public DevChatCommandTabCompleter(HoloUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if(args.length == 1){
            return commands;
        }

        return null;
    }
}
