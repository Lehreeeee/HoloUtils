package me.lehreeeee.HoloUtils.managers;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.reroll.RerollRequirement;
import me.lehreeeee.HoloUtils.reroll.RerollRequirementValidator;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RerollManager {
    private static RerollManager instance;
    private final HoloUtils plugin;
    private final Logger logger;

    private final Map<String, List<RerollRequirement>> rerollableItems = new HashMap<>();

    private static Economy econ;

    private RerollManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static RerollManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RerollManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new RerollManager(plugin);

            if (plugin.getServer().getPluginManager().getPlugin("Vault") != null){
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) econ = rsp.getProvider();
            }
        }
    }

    public void loadRerollConfig(YamlConfiguration rerollConfig){
        rerollableItems.clear();

        for(String key : rerollConfig.getKeys(false)){
            ConfigurationSection itemSection = rerollConfig.getConfigurationSection(key);
            if(itemSection == null) continue;

            String type = itemSection.getString("Type");
            String id = itemSection.getString("Id");

            if(type == null || id == null) {
                logger.warning("Invalid reroll entry at - " + key);
                continue;
            }

            List<RerollRequirement> requirements = new ArrayList<>();
            List<String> reqList = itemSection.getStringList("Requirements");

            for (String req : reqList) {
                requirements.add(new RerollRequirement(req));
            }

            String entry = type + ":" + id;
            rerollableItems.put(entry, requirements);

            logger.info("Loaded rerollable item entry: " + entry);
            for (RerollRequirement requirement : requirements) {
                logger.info("  " + requirement);
            }
        }
    }

    public ItemStack reroll(ItemStack itemStack, Player player){
        NBTItem nbtItem = NBTItem.get(itemStack);
        String entry = "";

        if(nbtItem.hasType()){
            entry = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");
        }

        if(!rerollableItems.containsKey(entry) || !RerollRequirementValidator.validateAllRequirements(rerollableItems.get(entry),player)) {
            return null;
        }

        MMOItemReforger reforger = new MMOItemReforger(nbtItem);
        reforger.reforge(MMOItems.plugin.getLanguage().revisionOptions);

        return reforger.getResult();
    }

    public List<Component> getDefaultDiceLore(){
        return List.of(
                MessageHelper.process("<aqua>Place an item to check"),
                MessageHelper.process("<aqua>reroll requirements.")
        );
    }

    public List<Component> getRequirementsLoreList(String entry, Player player){
        if(!rerollableItems.containsKey(entry)){
            return List.of(MessageHelper.process("<red>You cannot reroll any stat on this item."));
        } else {
            List<RerollRequirement> requirementsList = rerollableItems.get(entry);
            List<Component> requirementsLore = new ArrayList<>();

            for(RerollRequirement requirement : requirementsList) {
                requirementsLore.add(MessageHelper.process(RerollRequirementValidator.getValidatedLore(requirement,player)));
            }

            return requirementsLore;
        }
    }

    public Economy getEcon(){
        return econ;
    }
}
