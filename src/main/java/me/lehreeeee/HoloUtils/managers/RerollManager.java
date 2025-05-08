package me.lehreeeee.HoloUtils.managers;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.reroll.RerollRequirement;
import me.lehreeeee.HoloUtils.reroll.RerollRequirementValidator;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RerollManager {
    private static RerollManager instance;

    private final Map<String, List<RerollRequirement>> rerollableItems = new HashMap<>();

    private Economy econ;

    private RerollManager(){
        if (Bukkit.getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) this.econ = rsp.getProvider();
        }
    }

    public static RerollManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RerollManager not initialized.");
        }
        return instance;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new RerollManager();
        }
    }

    public void loadRerollConfig(YamlConfiguration rerollConfig){
        rerollableItems.clear();

        for(String key : rerollConfig.getKeys(false)){
            ConfigurationSection itemSection = rerollConfig.getConfigurationSection(key);
            if(itemSection == null) continue;

            String type = itemSection.getString("type");
            String id = itemSection.getString("id");

            if(type == null || id == null) {
                LoggerUtils.warning("Invalid reroll entry format at - " + key);
                continue;
            }

            List<RerollRequirement> requirements = new ArrayList<>();
            List<String> reqList = itemSection.getStringList("requirements");

            for (String req : reqList) {
                try{
                    requirements.add(new RerollRequirement(req));
                } catch (IllegalArgumentException e){
                    LoggerUtils.severe(e.getMessage() + " at - " + key);
                }
            }

            String entry = type + ":" + id;
            rerollableItems.put(entry, requirements);

            LoggerUtils.info("Loaded rerollable item entry: " + entry);
            for (RerollRequirement requirement : requirements) {
                LoggerUtils.info("  " + requirement);
            }
        }
    }

    public ItemStack reroll(ItemStack itemStack, Player player){
        NBTItem nbtItem = NBTItem.get(itemStack);
        String itemKey = "";

        if(nbtItem.hasType()){
            itemKey = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");
        }

        if(!rerollableItems.containsKey(itemKey) || !RerollRequirementValidator.validateAllRequirements(rerollableItems.get(itemKey),player)) {
            return null;
        }

        // Reroll starts
        // Add info into pdc to let ItemsUpdater know this is a reroll item, don't overwrite the random stats.
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("holoutils","reroll"), PersistentDataType.BOOLEAN, true);
        itemStack.setItemMeta(itemMeta);

        MMOItemReforger reforger = new MMOItemReforger(itemStack);
        reforger.reforge(MMOItems.plugin.getLanguage().revisionOptions);

        return reforger.getResult();
    }

    public List<Component> getDefaultDiceLore(){
        return List.of(
                MessageHelper.process("<aqua>Place an item to check"),
                MessageHelper.process("<aqua>reroll requirements.")
        );
    }

    public List<Component> getRequirementsLoreList(String itemKey, Player player){
        if(!rerollableItems.containsKey(itemKey)){
            return List.of(MessageHelper.process("<red>You cannot reroll any stat on this item."));
        } else {
            List<Component> requirementsLore = new ArrayList<>();
            requirementsLore.add(MessageHelper.process("<aqua>Requirements:"));

            for(RerollRequirement requirement : rerollableItems.get(itemKey)) {
                requirementsLore.add(MessageHelper.process(RerollRequirementValidator.getValidatedLore(requirement,player)));
            }

            return requirementsLore;
        }
    }

    public Economy getEcon(){
        return econ;
    }

    public boolean isRerollable(String itemKey){
        return rerollableItems.containsKey(itemKey);
    }
}
