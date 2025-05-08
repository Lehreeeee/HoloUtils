package me.lehreeeee.HoloUtils.hooks;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.mount.controller.MountControllerTypes;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.UUID;

public class ModelEngineHook {

    public static void mountStatusDisplay(Entity targetEntity, TextDisplay display){
        // It may have multiple models applied to it. Get all and try to find anyone with statuseffect seat.
        Collection<ActiveModel> activeModels = ModelEngineAPI.getModeledEntity(targetEntity).getModels().values();
        LoggerUtils.debug(MessageFormat.format("Found {0} active models for this entity.", activeModels.size()));

        boolean foundSeat = false;
        for(ActiveModel activeModel : activeModels){
            MountManager mountManager = activeModel.getMountManager().orElse(null);
            if (mountManager != null && mountManager.getSeats().containsKey("statuseffect")) {
                // smh, the model become not rideable after a server restart. Guess i will just force set to true :)
                mountManager.setCanRide(true);

                LoggerUtils.debug("Found statuseffect seat, mounting the display.");
                mountManager.mountPassenger("statuseffect", display, MountControllerTypes.WALKING);
                display.setVisibleByDefault(true);

                // Exit after first statuseffect seat is found, it will be used to attach status display.
                foundSeat = true;
                break;
            }
        }

        // statuseffect seat not found, sends a warning to tell dev to add it and do not show it (To avoid player confusion)
        if(!foundSeat){
            LoggerUtils.warning("Unable to find a seat to mount status display for entity - " + targetEntity.getName()
                    + ". Please follow the instructions in /HoloUtils/DisplayTag/StatusEffects.yml to fix this.");
        }
    }

    public static boolean isModeledEntity(UUID uuid){
        return ModelEngineAPI.isModeledEntity(uuid);
    }
}
