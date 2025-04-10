package me.lehreeeee.HoloUtils.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.lehreeeee.HoloUtils.utils.LoggerUtil;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DevChatManager {
    private static DevChatManager instance;

    private String devChatPrefix = "<aqua>[<red>Dev<aqua>]";
    private String devChatColor = "<aqua>";

    private final List<UUID> toggledOnDev = new ArrayList<>();

    public static DevChatManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("DevChatManager not initialized.");
        }
        return instance;
    }

    public static void initialize(){
        if (instance == null) {
            instance = new DevChatManager();
        }
    }

    public void loadDevChatConfig(ConfigurationSection devChatConfig){
        devChatPrefix = devChatConfig != null ? devChatConfig.getString("prefix", "<aqua>[<red>Dev<aqua>]") : "<aqua>[<red>Dev<aqua>]";
        devChatColor = devChatConfig != null ? devChatConfig.getString("color", "<aqua>") : "<aqua>";

        if(devChatConfig == null){
            LoggerUtil.info("Dev chat config section not found, using default configs.");
        }
    }

    public boolean toggleDevChat(UUID uuid, boolean on){
        if(on){
            toggledOnDev.add(uuid);
            return true;
        } else {
            toggledOnDev.remove(uuid);
            return false;
        }
    }

    public boolean hasDevChatOn(UUID uuid){
        return toggledOnDev.contains(uuid);
    }

    public void publishMessage(CommandSender sender, String message){
        // Default sender is yagoo, most likely console sender
        String messageSender = "<gold>Yagoo";

        // Update sender if its player
        if(sender instanceof Player player){
            messageSender = MessageHelper.revert(player.displayName());

            // TODO: Maybe add [item] for showing item in chat when hovered
            //if(message.contains("[item]")){
            //    ItemStack itemHeld = player.getInventory().getItemInMainHand();
            //    Component itemHover = Component.text("[item]").hoverEvent(itemHeld);
            //    logger.info(itemHover.toString());
//
            //    String serializedComponent = GsonComponentSerializer.gson().serialize(itemHover);
            //    message = message.replace("[item]", serializedComponent);
            //}
        }

        JsonObject json = new JsonObject();
        json.addProperty("messageSender", messageSender);
        json.addProperty("message", message);

        RedisManager.getInstance().publish("holo-devchat", json.toString());
    }

    public void sendMessage(String data){
        try{
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            // Check if the required fields exist in the JSON
            if (!json.has("messageSender") || !json.has("message")) {
                LoggerUtil.warning("Missing required fields in the JSON data.");
                return;
            }

            // Process final message to be sent to admin
            Component finalMessage = MessageHelper.process(devChatPrefix + "<aqua>["
                    + json.get("messageSender").getAsString() + "<reset><aqua>] " + devChatColor
                    + json.get("message").getAsString().replace("\\<", "<"));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("holoutils.devchat")) {
                    player.sendMessage(finalMessage);
                }
            }

            LoggerUtil.info(MessageHelper.getPlainText(MessageHelper.revert(finalMessage)));
        } catch (JsonSyntaxException e){
            LoggerUtil.warning("Invalid JSON format received from devchat channel - " + data);
        }
    }
}
