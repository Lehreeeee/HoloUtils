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
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private PersistentDataContainer pdc;

    public ItemPDCEditor(HoloUtils plugin, ItemStack itemStack){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
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

    public boolean removeData(String key){
        NamespacedKey nsk = NamespacedKey.fromString(key);
        if (nsk == null || !pdc.has(nsk)) return false;

        // Remove from pdc then update item meta
        pdc.remove(nsk);
        itemStack.setItemMeta(itemMeta);

        return true;
    }

    public boolean setData(String namespace, String key, String dataType, String data){
        NamespacedKey nsk = NamespacedKey.fromString(namespace + ":" + key);
        if(nsk == null) return false;

        try{
            switch (dataType.toUpperCase()) {
                case "STRING":
                    pdc.set(nsk,PersistentDataType.STRING, data);
                    break;
                case "INTEGER":
                    pdc.set(nsk,PersistentDataType.INTEGER, Integer.valueOf(data));
                    break;
                case "FLOAT":
                    pdc.set(nsk,PersistentDataType.FLOAT, Float.valueOf(data));
                    break;
                case "DOUBLE":
                    pdc.set(nsk,PersistentDataType.DOUBLE, Double.valueOf(data));
                    break;
                case "LONG":
                    pdc.set(nsk,PersistentDataType.LONG, Long.valueOf(data));
                    break;
                case "BYTE":
                    pdc.set(nsk,PersistentDataType.BYTE, Byte.valueOf(data));
                    break;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }

        itemStack.setItemMeta(itemMeta);
        return true;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    private void debugLogger(String debugMessage){
        if(plugin.shouldPrintDebug()) logger.info(debugMessage);
    }
}
