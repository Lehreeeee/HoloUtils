package me.lehreeeee.HoloUtils.utils;

import me.lehreeeee.HoloUtils.HoloUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class LoggerUtils {
    private static final Logger logger = HoloUtils.plugin.getLogger();

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void debug(String message) {
        if (HoloUtils.debug) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void file(String folderName, String message) {
        folderName = folderName + "/logs";
        File folder = new File(HoloUtils.plugin.getDataFolder() , folderName);

        // Ensure the folder exists
        if(folder.mkdirs()){
            debug("Created log folder: " + folderName);
        }

        File file = new File(folder, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))){
            // Create the file if it doesn't exist
            if(file.createNewFile()){
                debug("Created log file: " + folderName + "/" + file.getName());
            }

            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

            writer.write("[" + timeStamp + "] " + message);
            writer.newLine();
        } catch(IOException e){
            logger.severe("Failed to write to file: " + folderName + "/" + file.getName());
            throw new RuntimeException(e);
        }
    }
}
