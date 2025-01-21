package me.lehreeeee.HoloUtils.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class DevChatManager {
    private static DevChatManager instance;
    private final HoloUtils plugin;
    private final Logger logger;
    private final List<UUID> toggledOnDev = new ArrayList<>();
    private String prefix = "<aqua>[<red>Dev<aqua>]";

    private DevChatManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static DevChatManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("DevChatManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new DevChatManager(plugin);
        }
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public boolean toggleDevChat(UUID uuid, boolean on){
        if(on){
            toggledOnDev.add(uuid);
            return true;
        } else{
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
                logger.warning("Missing required fields in the JSON data.");
                return;
            }

            // Process final message to be sent to admin
            Component finalMessage = MessageHelper.process(prefix + "<aqua>["
                    + json.get("messageSender").getAsString() + "<reset><aqua>] "
                    + json.get("message").getAsString().replace("\\<", "<"));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("holoutils.devchat")) {
                    player.sendMessage(finalMessage);
                }
            }
        } catch (JsonSyntaxException e){
            logger.warning("Invalid JSON format received from devchat channel - " + data);
        }
    }
}
