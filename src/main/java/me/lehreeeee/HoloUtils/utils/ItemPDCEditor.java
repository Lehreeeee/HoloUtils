package me.lehreeeee.HoloUtils.utils;

import me.lehreeeee.HoloUtils.HoloUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ItemPDCEditor {
    private final HoloUtils plugin;
    private final Logger logger;
    private final ItemStack preEditItem;
    private final ItemMeta itemMeta;
    private PersistentDataContainer pdc;

    public ItemPDCEditor(HoloUtils plugin, ItemStack preEditItem){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.preEditItem = preEditItem;
        this.itemMeta = preEditItem.getItemMeta();
        if(itemMeta != null) this.pdc = itemMeta.getPersistentDataContainer();
    }

    public List<String> getNSKs(){
        if(pdc == null) return null;

        List<String> NSKs = new ArrayList<>();

        for(NamespacedKey nsk : pdc.getKeys()){
            NSKs.add(nsk.toString());
        }

        return NSKs;
    }

    public String getData(String key){
        NamespacedKey nsk = NamespacedKey.fromString(key);

        if (nsk == null) return null;

        List<PersistentDataType<?, ?>> possibleDataTypes = List.of(
                PersistentDataType.BYTE,
                PersistentDataType.SHORT,
                PersistentDataType.INTEGER,
                PersistentDataType.LONG,
                PersistentDataType.FLOAT,
                PersistentDataType.DOUBLE,
                PersistentDataType.STRING,
                PersistentDataType.BYTE_ARRAY,
                PersistentDataType.INTEGER_ARRAY,
                PersistentDataType.LONG_ARRAY,
                PersistentDataType.TAG_CONTAINER
        );

        // Go thru all types, find correct datatype & return the data
        for(PersistentDataType<?, ?> dataType : possibleDataTypes){
            try {
                Object result = pdc.get(nsk, dataType);
                if (result != null) {
                    String className = result.getClass().getName();

                    if (className.startsWith("java.lang.")) {
                        className = className.substring("java.lang.".length());
                        debugLogger("Data found with type: " + className);
                    } else {
                        debugLogger("Data found with type: " + className);
                    }

                    return result + " (" + className + ")";
                }
            } catch (IllegalArgumentException e) {
                // Ignored: Type mismatch, trying the next type
            }
        }

        // Found nothing
        return null;
    }

    private void debugLogger(String debugMessage){
        if(plugin.shouldPrintDebug()) logger.info(debugMessage);
    }
}
