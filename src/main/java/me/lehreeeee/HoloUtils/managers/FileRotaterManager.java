package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.fileRotater.CycleChecker;
import me.lehreeeee.HoloUtils.fileRotater.CycledFileStruct;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileRotaterManager {

    private static final String CONFIG_FILE_NAME = "FileRotater.yml";
    private static final String CYCLED_FOLDER_PATH = "Targets/%s/";
    private static final String CYCLED_FILE_PATH = "Targets/%s/%s";
    private static final String LOG_MESSAGE_REPLACED_FILE = "[FileRotater] Replaced %s with %s successfully!";
    private static final String WARNING_MISSING_CYCLED_FOLDER = "[FileRotater] Warning: Folder %s is missing!";
    private static final String WARNING_MISSING_CYCLED_FILE = "[FileRotater] Warning: File %s is missing in Folder %s!";
    private static final String ERROR_LOADING_CYCLED_FILES = "[FileRotater] Error loading Section %s in Config";

    private final JavaPlugin plugin;
    private final ArrayList<CycledFileStruct> cycledFiles;

    private List<String> commandsToExecute;
    private int intervalCheck;
    private CycleChecker cycleChecker;

    private static FileRotaterManager instance;

    public FileRotaterManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cycledFiles = new ArrayList<>();

        if(!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        if(!configFile.exists()) {
            loadFromResource(CONFIG_FILE_NAME, CONFIG_FILE_NAME);
            LoggerUtils.info("Successfully loaded " + CONFIG_FILE_NAME + " from Resources");
        }

        if(setupConfigFile(configFile)) {
            LoggerUtils.severe("Error Occurred in FileRotater. Startup Terminated.");
        }
    }

    public static FileRotaterManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FileRotater not initialized.");
        }
        return instance;
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new FileRotaterManager(plugin);
        }
    }

    public void update(CycledFileStruct cycledFile, int cycles) {
        //Replace file
        String cycledFilepath = String.format(CYCLED_FILE_PATH, cycledFile.getCycledFolderName(), cycledFile.getCurrRotationFileName());
        File oldFile = new File(plugin.getDataFolder().getParentFile(), cycledFile.getTargetPath());
        File newFile = new File(plugin.getDataFolder(), cycledFilepath);
        try {
            FileInputStream inputStream = new FileInputStream(newFile);
            FileOutputStream outputStream = new FileOutputStream(oldFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            LoggerUtils.info(String.format(LOG_MESSAGE_REPLACED_FILE, oldFile, newFile));
        } catch (IOException e) {
            LoggerUtils.debug(e.toString());
            LoggerUtils.debug("Targeted File Path: " + oldFile.getPath());
            LoggerUtils.debug("New File Path: " + newFile.getPath());
        }

        //Update config.yml
        String timeKey = String.format("Targets.%s.NextRotationTime", cycledFile.getCycledFolderName());
        long nextTime = cycledFile.getNextRotationTime() + (long) cycledFile.getInterval() * cycles;

        String fileKey = String.format("Targets.%s.CurrRotationFile", cycledFile.getCycledFolderName());
        int currIndex = cycledFile.getCycledFileNames().indexOf(cycledFile.getCurrRotationFileName());
        int nextIndex = (currIndex + cycles) % cycledFile.getCycledFileNames().size();
        String nextFileName = cycledFile.getCycledFileNames().get(nextIndex);

        cycledFile.updateCycle(nextTime, nextFileName);
        plugin.getConfig().set(timeKey, nextTime);
        plugin.getConfig().set(fileKey, nextFileName);
        plugin.saveConfig();
    }

    public List<String> getCommandsToExecute() {
        return commandsToExecute;
    }

    public ArrayList<CycledFileStruct> getCycledFiles() {
        return this.cycledFiles;
    }

    public void stop() {
        cycleChecker.cancel();
    }

    public void start() {
        cycledFiles.sort(Comparator.comparingLong(CycledFileStruct::getNextRotationTime));
        this.cycleChecker = new CycleChecker(this, plugin);
        cycleChecker.runTaskTimer(plugin, 0, intervalCheck * 20);
    }

    public void reload() {
        stop();
        File configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        if(setupConfigFile(configFile)) {
            LoggerUtils.severe("Error Occurred in FileRotater. Startup Terminated.");
        }
        start();
    }

    public ArrayList<String> getInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        ArrayList<String> infoList = new ArrayList<>();
        if(cycledFiles.isEmpty()) {
            infoList.add("Nothing here!");
            return infoList;
        }
        infoList.add("List of Files being Automatically Rotated by HoloUtils");
        infoList.add("======================================================");
        for(CycledFileStruct cycledFile : cycledFiles) {
            infoList.add(cycledFile.fetchInfo(currentTime));
        }
        infoList.add("======================================================");
        return infoList;
    }

    private boolean setupConfigFile(File configFile) {
        boolean errorExistFlag = false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        this.commandsToExecute = config.getStringList("CommandsExecutedOnRotate");
        this.intervalCheck = config.getInt("IntervalCheck");
        ConfigurationSection targetSection = config.getConfigurationSection("Targets");
        for(String key : targetSection.getKeys(false)) {
            if(parseCycledFiles(targetSection, key)) {
                errorExistFlag = true;
                LoggerUtils.severe(String.format(ERROR_LOADING_CYCLED_FILES, key));
            }
        }
        return errorExistFlag;
    }

    private boolean parseCycledFiles(ConfigurationSection mainSection, String cycledFolderName) {
        boolean errorExistFlag = false;

        ConfigurationSection section = mainSection.getConfigurationSection(cycledFolderName);
        int interval = section.getInt("Interval");
        long timestamp = section.getLong("NextRotationTime");
        String nextFile = section.getString("CurrRotationFile");
        String targetFilePath = section.getString("TargetFilePath");
        List<String> fileList = section.getStringList("Files");

        File cycledFolder =  new File(plugin.getDataFolder(), String.format(CYCLED_FOLDER_PATH, cycledFolderName));
        if(!cycledFolder.exists()) {
            LoggerUtils.warning(String.format(WARNING_MISSING_CYCLED_FOLDER, cycledFolder.getAbsolutePath()));
            errorExistFlag = true;
        }
        for(String cycledFileName : fileList) {
            File cycledFile = new File(plugin.getDataFolder(), String.format(CYCLED_FILE_PATH, cycledFolderName, cycledFileName));
            if(!cycledFile.exists()) {
                LoggerUtils.severe(String.format(WARNING_MISSING_CYCLED_FILE, cycledFileName, cycledFolderName));
                errorExistFlag = true;
            }
        }

        this.cycledFiles.add(new CycledFileStruct(interval, targetFilePath, cycledFolderName, fileList, timestamp, nextFile));
        return errorExistFlag;
    }

    private void loadFromResource(String fileName, String dest) {
        InputStream inputStream = plugin.getResource(fileName);
        assert inputStream != null;
        File createdFile = new File(plugin.getDataFolder(), dest);
        try {
            FileOutputStream outputStream = new FileOutputStream(createdFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();

        } catch (IOException e) {
            LoggerUtils.debug(e.toString());
        }
    }


}