
package me.lehreeeee.HoloUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MessageHelper {

    private static final String prefix = "<aqua>[<#FFA500>HoloUtils<aqua>] ";

    public static Component process(String msg) {
        return process(msg,false);
    }

    public static Component process(String msg, boolean needsPrefix) {
        return MiniMessage.miniMessage().deserialize(needsPrefix ? prefix + msg : msg);
    }

    public static String getPlainText(String msg) {
        return PlainTextComponentSerializer.plainText().serialize(process(msg));
    }
}
