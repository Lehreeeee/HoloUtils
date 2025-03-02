package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.reroll.RerollRequirement;
import me.lehreeeee.HoloUtils.reroll.RerollRequirementType;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

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
                logger.info("Requirement - " + req);
                requirements.add(new RerollRequirement(req));
            }

            String entry = type + ":" + id;
            rerollableItems.put(entry, requirements);

            logger.info("Loaded rerollable item entry: " + entry);
            for (RerollRequirement requirement : requirements) {
                logger.info("  - Requirement: " + requirement);
            }
        }
    }

    public List<Component> getDefaultDiceLore(){
        return List.of(
                MessageHelper.process("<!i><aqua>Place an item to check"),
                MessageHelper.process("<!i><aqua>reroll requirements.")
        );
    }

    public List<Component> getRequirementsLore(String entry){
        if(!rerollableItems.containsKey(entry)){
            return List.of(MessageHelper.process("<!i><red>You cannot reroll any stat on this item."));
        } else {
            List<RerollRequirement> requirementsList = rerollableItems.get(entry);
            List<Component> requirementsLore = new ArrayList<>();

            for(RerollRequirement requirement : requirementsList){
                RerollRequirementType requirementType = requirement.getRequirementType();

                switch(requirementType){
                    case MONEY -> requirementsLore.add(MessageHelper.process("<gold>Money"));
                    case MMOITEMS -> requirementsLore.add(MessageHelper.process("<gold>MMOItems"));
                    case RECURRENCY -> requirementsLore.add(MessageHelper.process("<gold>Recurrency"));
                    case null -> requirementsLore.add(MessageHelper.process("<red>Unsupported requirement type, check again!"));
                }
            }

            return requirementsLore;
        }
    }
}
