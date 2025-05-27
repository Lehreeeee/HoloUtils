
package me.lehreeeee.HoloUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    private static final String prefix = "<aqua>[<#FFA500>HoloUtils<aqua>] <#FFA500>";

    public static void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtils.info(getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(process(msg,true));
    }

    public static Component process(String msg) {
        return process(msg,false);
    }

    public static Component process(String msg, boolean needsPrefix) {
        msg = "<!i>" + replaceLegacyColorCode(msg);
        return MiniMessage.miniMessage().deserialize(needsPrefix ? prefix + msg : msg);
    }

    public static String revert(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static String getPlainText(String msg) {
        return PlainTextComponentSerializer.plainText().serialize(process(msg));
    }

    private static String replaceLegacyColorCode(String input) {
        input = input.replace("§", "&");

        String[][] colorMap = {
                {"&0", "<black>"}, {"&1", "<dark_blue>"}, {"&2", "<dark_green>"}, {"&3", "<dark_aqua>"},
                {"&4", "<dark_red>"}, {"&5", "<dark_purple>"}, {"&6", "<gold>"}, {"&7", "<gray>"},
                {"&8", "<dark_gray>"}, {"&9", "<blue>"}, {"&a", "<green>"}, {"&b", "<aqua>"},
                {"&c", "<red>"}, {"&d", "<light_purple>"}, {"&e", "<yellow>"}, {"&f", "<white>"},
                {"&k", "<obfuscated>"}, {"&l", "<bold>"}, {"&m", "<strikethrough>"}, {"&n", "<underline>"},
                {"&o", "<italic>"}, {"&r", "<reset>"}
        };

        // Loop through and replace all occurrences of legacy codes with MiniMessage syntax
        for (String[] color : colorMap) {
            input = input.replace(color[0], color[1]);
        }

        return input;
    }
}
