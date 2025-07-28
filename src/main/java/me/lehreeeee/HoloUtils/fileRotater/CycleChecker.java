package me.lehreeeee.HoloUtils.fileRotater;

import me.lehreeeee.HoloUtils.managers.FileRotaterManager;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;

public class CycleChecker extends BukkitRunnable {

    public static final String LOG_FILE_ROTATED = "[FileRotater] Cycled %d times for %s at time %d";
    private FileRotaterManager fileRotater;
    private JavaPlugin plugin;

    public CycleChecker(FileRotaterManager fileRotater, JavaPlugin plugin) {
        this.fileRotater = fileRotater;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long currTime = System.currentTimeMillis() / 1000;
        long nextTime = fileRotater.getCycledFiles().getFirst().getNextRotationTime();
        if(currTime < nextTime) {
            return;
        }
        ArrayList<CycledFileStruct> cycledFiles = fileRotater.getCycledFiles();
        for(CycledFileStruct cycledFile : cycledFiles) {
            long endTime = cycledFile.getNextRotationTime();
            if(endTime > currTime) {
                break;
            }
            int cycles = calculateCycles(endTime, currTime, cycledFile.getInterval());
            fileRotater.update(cycledFile, cycles);
            LoggerUtils.info(String.format(LOG_FILE_ROTATED, cycles, cycledFile.getCycledFolderName(), currTime));
        }
        cycledFiles.sort(Comparator.comparingLong(CycledFileStruct::getNextRotationTime));
        for(String peko : fileRotater.getCommandsToExecute()) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), peko));
        }

    }

    private int calculateCycles(long expiryTime, long currentTime, int intervalDuration) {
        return (int) ((currentTime - expiryTime) / intervalDuration) + 1;
    }
}

