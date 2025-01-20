package me.lehreeeee.HoloUtils.RedisPubSub;

import me.lehreeeee.HoloUtils.HoloUtils;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RewardSubscriber {
    private final HoloUtils plugin;
    private Jedis redis;

    public RewardSubscriber(HoloUtils plugin) {
        this.plugin = plugin;
        redis = new Jedis("localhost", 6379);
        startSubscriber();
    }

    private void startSubscriber() {

    }
}
