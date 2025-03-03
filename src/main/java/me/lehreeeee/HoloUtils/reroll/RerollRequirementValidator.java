package me.lehreeeee.HoloUtils.reroll;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.managers.RerollManager;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RerollRequirementValidator {

    public static String getRequirementLore(RerollRequirement req) {
        RerollRequirementType requirementType = req.getRequirementType();
        Map<String, String> parameters = req.getParameters();

        switch (requirementType) {
            case MONEY -> {
                if(!parameters.containsKey("amount"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";

                req.setValid(true);
                return "<green>$" + parseDoubleSafe(parameters.get("amount"));
            }
            case MMOITEMS -> {
                if(!parameters.containsKey("amount") || !parameters.containsKey("type") || !parameters.containsKey("id"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";

                // Does this item exist?
                ItemStack mmoItem = MMOItems.plugin.getItem(MMOItems.plugin.getTypes().get(parameters.get("type")), parameters.get("id"));
                if(mmoItem == null)
                    return "<red>Invalid item requirement" + ", please report to developer!";

                // Return the item display name as lore
                req.setValid(true);
                return NBTItem.get(mmoItem).getString("MMOITEMS_NAME") + " <!i><!b><gold>x<green>" + parseIntegerSafe(parameters.get("amount"));
            }
            case RECURRENCY -> {
                if(!parameters.containsKey("amount") || !parameters.containsKey("currency"))
                    return "<red>Missing required parameters for " + requirementType + ", please report to developer!";

                req.setValid(true);
                return "<yellow>" + parameters.get("currency") + " <gold>x<green>" + parameters.get("amount");
            }
            case null -> {
                return "<red>Unsupported requirement type, please report to developer!";
            }
        }
    }

    public static String getValidatedLore(RerollRequirement req, Player player) {
        // Invalid? Return the base requirement lore that should contain error message.  (Due to config error)
        if(!req.isValid()) return req.getRequirementLore();

        boolean meetsRequirement;

        switch (req.getRequirementType()) {
            case MONEY -> meetsRequirement = checkMoneyRequirement(req,player,false);
            case MMOITEMS -> meetsRequirement = checkMMOItemsRequirement(req,player,false);
            case RECURRENCY -> meetsRequirement = checkRecurrencyRequirement(req,player,false);
            case null -> {
                return req.getRequirementLore();
            }
        }
        return toSymbol(meetsRequirement) + req.getRequirementLore();
    }

    public static boolean validateAllRequirements(List<RerollRequirement> requirements, Player player){
        // Return immediately if any of them is invalid
        for(RerollRequirement req : requirements){
            if(!req.isValid()) return false;

            switch (req.getRequirementType()) {
                case MONEY -> { if(!checkMoneyRequirement(req, player,true)) return false; }
                case MMOITEMS -> { if(!checkMMOItemsRequirement(req, player,true)) return false; }
                case RECURRENCY -> { if(!checkRecurrencyRequirement(req, player,true)) return false; }
                case null, default -> { return false; }
            }
        }
        return true;
    }

    private static boolean checkMoneyRequirement(RerollRequirement req, Player player, boolean shouldConsume) {
        Economy econ = RerollManager.getInstance().getEcon();
        double amount = parseDoubleSafe(req.getParameters().get("amount"));

        if(econ == null || !econ.has(player,amount)){
            return false;
        }

        if(shouldConsume){
            econ.withdrawPlayer(player,amount);
        }
        return true;
    }

    private static boolean checkMMOItemsRequirement(RerollRequirement req, Player player, boolean shouldConsume) {
        int totalAmount = 0;
        Inventory playerInv = player.getInventory();
        List<ItemStack> validItems = new ArrayList<>();

        for (ItemStack item : playerInv.getContents()) {
            if (item == null) continue;

            NBTItem nbtItem = NBTItem.get(item);
            if (Objects.equals(nbtItem.getType(), req.getParameters().get("type")) && Objects.equals(nbtItem.getString("MMOITEMS_ITEM_ID"), req.getParameters().get("id"))) {
                totalAmount += item.getAmount();
                validItems.add(item);
            }
        }

        int amount = parseIntegerSafe(req.getParameters().get("amount"));
        // Not enough, return early
        if(totalAmount < amount) return false;

        if(shouldConsume){
            for (ItemStack item : validItems) {
                int stackSize = item.getAmount();

                if (stackSize <= amount) {
                    // Remove and deduct the amount
                    playerInv.remove(item);
                    amount -= stackSize;
                } else {
                    // Final stack, decrease the stack size and then exit
                    item.setAmount(stackSize - amount);
                    break;
                }
            }
        }
        return true;
    }

    private static boolean checkRecurrencyRequirement(RerollRequirement req, Player player, boolean shouldConsume){
        boolean meetsRequirement = false;


        return meetsRequirement;
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }

    private static int parseIntegerSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private static String toSymbol(boolean meetsRequirement) {
        return meetsRequirement ? "<green>✔ " : "<red>✖ ";
    }
}

