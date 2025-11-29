package me.lehreeeee.HoloUtils.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.lehreeeee.HoloUtils.utils.ItemUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DevChatManager {
    private static DevChatManager instance;

    private String devChatPrefix = "<aqua>[<red>Dev<aqua>]";
    private String devChatColor = "<aqua>";

    private final Set<UUID> toggledOnDev = new HashSet<>();

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
            LoggerUtils.info("Dev chat config section not found, using default configs.");
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
        String encodedItem = "";

        // Update sender if its player
        if(sender instanceof Player player){
            messageSender = MessageUtils.revert(player.displayName());

            message = message.replace("[i]","[item]");

            if(message.contains("[item]")){
                ItemStack itemHeld = player.getInventory().getItemInMainHand();

                if(!itemHeld.getType().isAir())
                    encodedItem = ItemUtils.encodeItem(itemHeld);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("messageSender", messageSender);
        json.addProperty("message", message);
        json.addProperty("encodedItem", encodedItem);

        RedisManager.getInstance().publish("holo-devchat", json.toString());
    }

    public void sendMessage(String data){
        try{
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            // Check if the required fields exist in the JSON
            if (!json.has("messageSender") || !json.has("message")) {
                LoggerUtils.warning("Missing required fields in the JSON data.");
                return;
            }

            Component itemComp;
            Component messageComponent;

            String message = json.get("message").getAsString();

            String logMessage = devChatPrefix
                    + "<aqua>[" + json.get("messageSender").getAsString() + "<reset><aqua>] "
                    + devChatColor + message;

            if(message.contains("[item]") && json.has("encodedItem")){
                String encoded = json.get("encodedItem").getAsString();

                if(!encoded.isEmpty()) {
                    ItemStack item = ItemUtils.decodeItem(encoded);
                    ItemMeta itemMeta = item.getItemMeta();
                    Component displayName = itemMeta.hasDisplayName() ?
                            itemMeta.displayName() : Component.translatable(item);
                    int itemAmount = item.getAmount();
                    if(itemAmount > 1)
                        displayName = displayName.append(MessageUtils.process("<gold> x" + itemAmount));

                    itemComp = MessageUtils.process("<white>[" + MessageUtils.revert(displayName) + "<reset><white>]").hoverEvent(item);

                    String[] parts = message.split("\\[item\\]", -1);
                    messageComponent = Component.empty();
                    for (int i = 0; i < parts.length; i++) {
                        messageComponent = messageComponent.append(Component.text(parts[i]));
                        if (i != parts.length - 1) {
                            messageComponent = messageComponent.append(itemComp);
                        }
                    }
                    logMessage = logMessage.replace("[item]", "[" + MessageUtils.revert(displayName) + "]");
                } else {
                    messageComponent = MessageUtils.process(message.replace("\\<", "<"));
                }
            } else {
                messageComponent = MessageUtils.process(message.replace("\\<", "<"));
            }

            Component finalMessage = MessageUtils.process(devChatPrefix)
                    .append(MessageUtils.process("<aqua>[" + json.get("messageSender").getAsString() + "<reset><aqua>] " + devChatColor))
                    .append(messageComponent);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("holoutils.devchat")) {
                    player.sendMessage(finalMessage);
                }
            }

            LoggerUtils.info(MessageUtils.getPlainText(logMessage));
        } catch (JsonSyntaxException e){
            LoggerUtils.warning("Invalid JSON format received from devchat channel - " + data);
        }
    }
}
