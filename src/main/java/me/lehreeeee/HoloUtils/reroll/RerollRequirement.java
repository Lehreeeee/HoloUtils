package me.lehreeeee.HoloUtils.reroll;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.managers.RerollManager;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RerollRequirement {
    private final RerollRequirementType requirementType;
    private final Map<String, String> parameters;

    public RerollRequirement(String req) {
        if (req == null || !req.contains("{") || !req.contains("}")) {
            throw new IllegalArgumentException("Invalid requirement format: " + req);
        }

        String[] parts = req.split("\\{", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid requirement format: " + req);
        }

        this.requirementType = RerollRequirementType.fromString(parts[0].trim());
        this.parameters = parseParameters(parts[1].replace("}", "").trim());
    }

    public RerollRequirementType getRequirementType() {
        return requirementType;
    }

    public String getRequirementLore(Player player) {
        switch (requirementType) {
            case MONEY -> {
                if(!parameters.containsKey("amount"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";

                double amount = parseDoubleSafe(parameters.get("amount"));
                return checkMoneyRequirement(player,amount) + "<green>$" + amount;
            }
            case MMOITEMS -> {
                if(!parameters.containsKey("amount") || !parameters.containsKey("type") || !parameters.containsKey("id"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";

                // Does this item exist?
                ItemStack mmoItem = MMOItems.plugin.getItem(MMOItems.plugin.getTypes().get(parameters.get("type")), parameters.get("id"));
                if(mmoItem == null)
                    return "<red>Invalid item requirement" + ", please report to developer!";

                // Return the item display name as lore
                int amount = parseIntegerSafe(parameters.get("amount"));
                return checkMMOItemsRequirement(player,amount,parameters.get("type"),parameters.get("id"))
                        + "<!i>" + NBTItem.get(mmoItem).getString("MMOITEMS_NAME")
                        + " <!i><!b><gold>x<green>" + parameters.get("amount");
            }
            case RECURRENCY -> {
                if(!parameters.containsKey("amount") || !parameters.containsKey("currency"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";
                else
                    return "<yellow>" + parameters.get("currency") + " <gold>x<green>" + parameters.get("amount");
            }
            case null -> {
                return "<red>Unsupported requirement type, please report to developer!";
            }
        }
    }

    private Map<String, String> parseParameters(String rawParams) {
        Map<String, String> paramMap = new HashMap<>();
        String[] pairs = rawParams.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            }
        }
        return paramMap;
    }

    private String checkMoneyRequirement(Player player, double amount){
        boolean meetsRequirement = false;

        Economy econ = RerollManager.getInstance().getEcon();
        if(econ != null) {
            meetsRequirement = econ.has(player,amount);
        }

        return meetsRequirement ? "<green>✔ " : "<red>✖ ";
    }

    private String checkMMOItemsRequirement(Player player, int amount, String type, String id){
        int totalAmount = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if(item == null) continue;

            NBTItem nbtItem = NBTItem.get(item);
            if(nbtItem.hasType() &&  nbtItem.getType().equals(type) && nbtItem.getString("MMOITEMS_ITEM_ID").equals(id)){
                totalAmount += item.getAmount();
            }
        }

        return totalAmount >= amount ? "<green>✔ " : "<red>✖ ";
    }

    private String checkRecurrencyRequirement(Player player, Double amount){
        boolean meetsRequirement = false;


        return meetsRequirement ? "<green>✔ " : "<red>✖ ";
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }

    private int parseIntegerSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "RequirementType: " + requirementType + ", Params: " + this.parameters;
    }
}
