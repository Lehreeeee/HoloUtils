package me.lehreeeee.HoloUtils.hooks;

import com.Zrips.CMI.CMI;

import java.util.UUID;

public class CMIHook {

    public static boolean isVanished(UUID uuid){
        return CMI.getInstance().getPlayerManager().getUser(uuid).isCMIVanished();
    }
}
