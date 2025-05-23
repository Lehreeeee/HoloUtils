package me.lehreeeee.HoloUtils.leaderboard;

import me.lehreeeee.HoloUtils.utils.LoggerUtils;

import java.text.MessageFormat;
import java.util.*;

public class DamageLeaderboard {
    private final Map<UUID, Double> damageMap = new HashMap<>();
    private double totalDamage = 0.0;
    private long startTime = 0;
    private long endTime = 0;
    private boolean ended = false;

    public void addDamage(UUID uuid, double damage){
        if(damage <= 0) return;

        damageMap.merge(uuid,damage,Double::sum);
        totalDamage = totalDamage + damage;
        LoggerUtils.debug(MessageFormat.format("{0} dealt {1} damage.",uuid,damage));

        if(startTime == 0){
            LoggerUtils.debug("Initializing leaderboard start time to record fight duration.");
            startTime = System.currentTimeMillis();
        }
    }

    public void reset(){
        startTime = 0;
        damageMap.clear();
    }

    public void end(){
        if (!ended) {
            endTime = System.currentTimeMillis();
            ended = true;
        }
    }

    public double getDamage(UUID uuid) {
        return damageMap.getOrDefault(uuid, 0.0);
    }

    public long getDuration(){
        if(startTime == 0) return 0;

        return (ended ? endTime : System.currentTimeMillis()) - startTime;
    }

    public List<Map.Entry<UUID,Double>> getSorted(){
        return damageMap.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();
    }
}
