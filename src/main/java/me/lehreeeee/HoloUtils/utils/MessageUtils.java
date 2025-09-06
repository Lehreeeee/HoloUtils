
package me.lehreeeee.HoloUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

public class MessageUtils {

    private static final String PREFIX = "<aqua>[<gold>{0}<aqua>] <gold>";
    private static final String DEFAULT_MODULE = "HoloUtils";

    public static void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtils.info(getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(process(msg,true,DEFAULT_MODULE));
    }

    public static void sendFeedbackMessage(CommandSender sender, String msg, String module){
        LoggerUtils.info(getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(process(msg,true,module));
    }

    /**
     * Processes a message into a MiniMessage component. Without any prefix.
     *
     * @param msg the raw message text
     * @return the deserialized MiniMessage component
     */
    public static Component process(String msg) {
        return process(msg,false);
    }

    /**
     * Processes a message into a MiniMessage component, with optional prefix support.
     * <p>By default prefix is {@code HoloUtils}, check see also to change the prefix
     *
     * @param msg the raw message text
     * @param needsPrefix whether to prepend the prefix
     * @return the deserialized MiniMessage component
     *
     * @see #process(String, boolean, String) Custom prefix
     */
    public static Component process(String msg, boolean needsPrefix) {
        return process(msg,needsPrefix,DEFAULT_MODULE);
    }

    /**
     * Processes a message into a MiniMessage component, with optional prefix and module name.
     * <p>Prefix: {@literal "<aqua>[<gold>{module}<aqua>] <gold>"}
     *
     * @param msg the raw message text
     * @param needsPrefix whether to prepend the prefix
     * @param module the module name for prefix formatting
     * @return the deserialized MiniMessage component
     */
    public static Component process(String msg, boolean needsPrefix, String module) {
        msg = "<!i>" + replaceLegacyColorCode(msg);
        return MiniMessage.miniMessage().deserialize(needsPrefix ? MessageFormat.format(PREFIX,module) + msg : msg);
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
