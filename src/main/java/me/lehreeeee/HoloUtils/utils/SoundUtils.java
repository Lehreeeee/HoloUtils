package me.lehreeeee.HoloUtils.utils;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SoundUtils {

    public static void playSound(Player player, String sound){
        playSound(player,sound,null,null,null);
    }

    public static void playSound(Player player, String sound, @Nullable Sound.Source source, @Nullable Float volume, @Nullable Float pitch){
        try{
            player.playSound(Sound.sound(
                    Key.key(sound),
                    source == null ? Sound.Source.MASTER : source,
                    volume == null ? 1.0F : volume,
                    pitch == null ? 1.0F : pitch));
        } catch (InvalidKeyException e){
            LoggerUtils.warning("Incorrect sound key: " + e.keyNamespace());
        }
    }
}
