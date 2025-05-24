package me.lehreeeee.HoloUtils.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lehreeeee.HoloUtils.managers.DamageLeaderboardManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "holoutils";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lehreeeee";
    }

    @Override
    public @NotNull String getVersion() {
        return "69.420.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    /*
        Available Placeholders:
        1. %holoutils_damagelb_entry_{<uuid>}_{<position>}%
        2. %holoutils_damagelb_damage_{<uuid>}_{<position>}%
        3. %holoutils_damagelb_damagep_{<uuid>}%
        4. %holoutils_damagelb_duration_{<uuid>}%
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        LoggerUtils.debug("Requesting holoutils placeholder: " + params);

        // Expecting format: "damagelb_<type>_{<uuid>}_{<position>}"
        if (params.startsWith("damagelb_")) {
            String[] segments = params.substring(9).split("_", 2);

            if (segments.length != 2) return "";

            // <type>
            String type = segments[0];
            // {<uuid>}_{<position>} OR {<uuid>}
            // {<uuid>}_{<position>}_simple OR {<uuid>}_simple
            String rawParams = segments[1];

            switch (type) {
                case "entry":
                case "damage": {
                    Pair<Map.Entry<UUID,Double>,Double> pair = getLeaderboardEntry(rawParams);
                    if(pair == null) return "";

                    Map.Entry<UUID,Double> entry = pair.getKey();
                    String value = String.valueOf(entry.getValue());

                    if(type.equals("entry")){
                        return Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    }

                    return rawParams.endsWith("_simple") ?
                            value : value + " ("+ pair.getValue() + "%)";
                }
                case "damagep":{
                    Pair<Double,Double> pair = getPlayerLeaderboardDamage(rawParams,player.getUniqueId());
                    String damage = String.valueOf(pair.getKey());

                    return rawParams.endsWith("_simple") ?
                            damage : damage + " ("+ pair.getValue() + "%)";
                }
                case "duration": {
                    return getLeaderboardDuration(rawParams);
                }
            }
        }
        return "";
    }

    private Pair<Double,Double> getPlayerLeaderboardDamage(String params, UUID playerUUID){
        try{
            if (!params.startsWith("{") || !params.endsWith("}")) {
                return Pair.of(0.0,0.0);
            }

            String[] parts = splitParams(params);
            if (parts.length != 1) return Pair.of(0.0,0.0);

            UUID uuid = UUID.fromString(parts[0]);

            return DamageLeaderboardManager.getInstance().getPlayerLeaderboardDamage(uuid,playerUUID);
        }  catch (Exception e) {
            return Pair.of(0.0,0.0);
        }
    }

    private Pair<Map.Entry<UUID,Double>,Double> getLeaderboardEntry(String params){
        try{
            if (!params.startsWith("{") || !params.contains("}_{") || !params.endsWith("}")) {
                return null;
            }

            String[] parts = splitParams(params);
            if (parts.length != 2) return null;

            UUID uuid = UUID.fromString(parts[0]);
            int position = Integer.parseInt(parts[1]);

            return DamageLeaderboardManager.getInstance().getLeaderboardEntry(uuid,position);
        } catch (Exception e) {
            return null;
        }
    }

    private String getLeaderboardDuration(String params){
        try{
            if (!params.startsWith("{") || !params.endsWith("}")) {
                return "";
            }

            String[] parts = splitParams(params);
            if (parts.length != 1) return "";

            UUID uuid = UUID.fromString(parts[0]);

            return DamageLeaderboardManager.getInstance().getLeaderboardDuration(uuid);
        } catch (Exception e) {
            return "";
        }
    }

    private String[] splitParams(String params){
        return params.substring(1, params.length() - 1).split("}_\\{");
    }
}
