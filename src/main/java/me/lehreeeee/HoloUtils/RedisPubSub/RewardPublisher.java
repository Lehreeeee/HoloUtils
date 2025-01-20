package me.lehreeeee.HoloUtils.RedisPubSub;

import me.lehreeeee.HoloUtils.HoloUtils;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RewardPublisher {
    private final HoloUtils plugin;
    private Jedis redis;

    public RewardPublisher(HoloUtils plugin){
        this.plugin = plugin;
        this.redis = new Jedis("localhost",6379);
    }

    public void publishReward(UUID uuid, String reward) {
        // Create a JSON message
        String message = "{\"uuid\":\"" + uuid + "\",\"reward\":\"" + reward + "\"}";
        // Publish the message to the "rewards" channel
        redis.publish("rewards", message);
    }

    public void close() {
        redis.close();
    }
}
