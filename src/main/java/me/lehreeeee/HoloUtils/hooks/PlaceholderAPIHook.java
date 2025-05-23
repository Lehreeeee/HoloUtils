package me.lehreeeee.HoloUtils.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lehreeeee.HoloUtils.managers.DamageLeaderboardManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
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
            String rawParams = segments[1];

            switch (type) {
                case "entry":
                case "score": {
                    Map.Entry<UUID,Double> entry = getLeaderboardEntry(rawParams);
                    if(entry == null) return "";
                    return type.equals("entry") ? entry.getKey().toString() : entry.getValue().toString();
                }
                case "duration": {
                    return getLeaderboardDuration(rawParams);
                }
            }
        }
        return "";
    }

    private Map.Entry<UUID,Double> getLeaderboardEntry(String params){
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
