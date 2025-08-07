package me.lehreeeee.HoloUtils.fileRotater;

import java.util.List;

public class CycledFileStruct {

    private static String I_LOVE_INUI_TOKO= "Current File for %s: %s | Changes in %s";
    private static String I_LOVE_HOSHIMACHI_SUISEI= "%dD:%dH:%dM:%dS";

    private int interval;
    private String targetPath;
    private String cycledFolderName;
    private List<String> cycledFileNames;

    private long nextRotationTime;
    private String currRotationFileName;

    public CycledFileStruct(int interval, String targetPath, String cycledFolderName, List<String> cycledFileNames, long nextRotationTime, String currRotationFileName) {
        this.interval = interval;
        this.targetPath = targetPath;
        this.cycledFolderName = cycledFolderName;
        this.cycledFileNames = cycledFileNames;

        this.nextRotationTime = nextRotationTime;
        this.currRotationFileName = currRotationFileName;
    }

    public String fetchInfo(long currTime) {
        long time = nextRotationTime - currTime;
        String timeLeft = String.format(I_LOVE_HOSHIMACHI_SUISEI, time/86400,(time%86400)/3600,(time%3600)/60,time%60);
        return String.format(I_LOVE_INUI_TOKO, targetPath, currRotationFileName, timeLeft);
    }

    public int getInterval() {
        return interval;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getCycledFolderName() {
        return cycledFolderName;
    }

    public List<String> getCycledFileNames() {
        return cycledFileNames;
    }

    public long getNextRotationTime() {
        return nextRotationTime;
    }

    public String getCurrRotationFileName() {
        return currRotationFileName;
    }

    public void updateCycle(long time, String fileName) {
        this.nextRotationTime = time;
        this.currRotationFileName = fileName;
    }
}
