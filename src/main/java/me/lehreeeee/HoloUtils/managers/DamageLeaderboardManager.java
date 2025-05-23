package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.leaderboard.DamageLeaderboard;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;

import java.time.Duration;
import java.util.*;

public class DamageLeaderboardManager {
    private static DamageLeaderboardManager instance;
    private final Map<UUID, DamageLeaderboard> trackedEntities = new HashMap<>();
    private final Map<UUID, DamageLeaderboard> endedLeaderboards = new HashMap<>();
    private final Map<UUID,UUID> linkedEntities = new HashMap<>();

    private DamageLeaderboardManager(){}

    public static DamageLeaderboardManager getInstance(){
        if(instance == null){
            instance = new DamageLeaderboardManager();
        }
        return instance;
    }

    public void addDamage(UUID victimUUID, UUID damagerUUID, double damage){
        DamageLeaderboard leaderboard = trackedEntities.get(linkedEntities.getOrDefault(victimUUID,victimUUID));

        if(leaderboard == null){
            LoggerUtils.severe("Failed to add damage to leaderboard, victim " + victimUUID + " has no leaderboard or parent with leaderboard.");
            return;
        }

        leaderboard.addDamage(damagerUUID,damage);
    }

    /**
     * Adds an entity to tracked list.
     * An entity that has parent cannot be added into the tracked list.
     *
     * @param uuid UUID of entity to be tracked
     * @return {@code false} if entity is already being tracked or has a parent, {@code true} if successfully added to tracked list.
     */
    public boolean trackEntity(UUID uuid) {
        if(trackedEntities.containsKey(uuid) || linkedEntities.containsKey(uuid)){
            return false;
        }

        trackedEntities.put(uuid,new DamageLeaderboard());
        return true;
    }

    /**
     * Removes an entity from the tracked list. Then, end the leaderboard of the entity if exists.
     * At the same time, remove all it's children.
     *
     * @param uuid UUID of entity to be untracked
     * @return {@code false} if entity is not being tracked.
     */
    public boolean untrackEntity(UUID uuid) {
        DamageLeaderboard leaderboard = trackedEntities.remove(uuid);
        if(leaderboard == null) {
            return false;
        }

        leaderboard.end();
        endedLeaderboards.put(uuid,leaderboard);

        // Does it have children? Remove all.
        if(linkedEntities.containsValue(uuid)){
            LoggerUtils.info("Found child for this entity, removing them from linked list.");

            Iterator<Map.Entry<UUID,UUID>> iterator = linkedEntities.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<UUID, UUID> entry = iterator.next();
                if(entry.getValue().equals(uuid)){
                    iterator.remove();
                    LoggerUtils.info("Removed children " + entry.getKey() + " from linked list.");
                }
            }
        }
        return true;
    }

    /**
     * Links a child entity to a parent entity.
     *
     * @param childUUID  the UUID of the child entity
     * @param parentUUID the UUID of the parent entity
     * @return {@code true} if an existing link was replaced; {@code false} if it was a new entry
     */
    public boolean linkEntity(UUID childUUID, UUID parentUUID){
        return linkedEntities.put(childUUID, parentUUID) != null;
    }

    public UUID unLinkEntity(UUID childUUID){
        return linkedEntities.remove(childUUID);
    }

    public Set<UUID> getTrackedEntities(){
        return trackedEntities.keySet();
    }

    public Set<Map.Entry<UUID, UUID>> getLinkedEntities(){
        return linkedEntities.entrySet();
    }

    public boolean isEntityTrackedOrLinked(UUID victimUUID){
        return (trackedEntities.containsKey(victimUUID) || linkedEntities.containsKey(victimUUID));
    }

    /**
     * Resets the leaderboard of the entity.
     *
     * @param uuid UUID of entity to be reset.
     * @return {@code false} if entity is not being tracked or fight as ended, {@code true} if successfully reset leaderboard.
     */
    public boolean resetLeaderboard(UUID uuid){
        DamageLeaderboard leaderboard = trackedEntities.get(uuid);
        if(leaderboard == null) {
            return false;
        }

        leaderboard.reset();
        return true;
    }

    public Map.Entry<UUID,Double> getLeaderboardEntry(UUID uuid, int position){
        DamageLeaderboard leaderboard = getLeaderboard(uuid);

        if(leaderboard == null) return null;

        try{
            return leaderboard.getSorted().get(position - 1);
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public String getLeaderboardDuration(UUID uuid) {
        DamageLeaderboard leaderboard = getLeaderboard(uuid);

        if(leaderboard == null) return "N/A";

        Duration duration = Duration.ofMillis(leaderboard.getDuration());

        return String.format("%02d:%02d:%02d.%03d",
                duration.toHours(),
                duration.toMinutes() % 60,
                duration.getSeconds() % 60,
                duration.toMillis() % 1000
        );
    }

    private DamageLeaderboard getLeaderboard(UUID uuid){
        DamageLeaderboard leaderboard = trackedEntities.get(uuid);
        if(leaderboard == null) {
            return endedLeaderboards.get(uuid);
        }
        return leaderboard;
    }
}
