package me.lehreeeee.HoloUtils.utils;

import me.lehreeeee.HoloUtils.HoloUtils;

import java.util.logging.Logger;

public class LoggerUtils {
    private static final Logger logger = HoloUtils.getPlugin().getLogger();

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
        if (HoloUtils.getPlugin().shouldPrintDebug()) {
            logger.info("[DEBUG] " + message);
        }
    }
}
