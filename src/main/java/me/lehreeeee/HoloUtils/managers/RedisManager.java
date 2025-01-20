package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.HoloUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.logging.Logger;

public class RedisManager {
    private static RedisManager instance;
    private final HoloUtils plugin;
    private final Logger logger;

    private RedisManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static RedisManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("RedisManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if (instance == null) {
            instance = new RedisManager(plugin);
        }
    }

    public void subscribe(){
        try(Jedis subscriber = new Jedis("localhost", 6379)){
            subscriber.connect();

            String[] channels = {"test", "test2"};
            logger.info("Subscribing to channels " + Arrays.toString(channels));

            new Thread("Holo-Redis-Subscriber"){
                @Override
                public void run(){

                    logger.info("Stared subscriber thread - " + this.getName());
                    subscriber.subscribe(new JedisPubSub(){
                        @Override
                        public void onMessage(String channel, String data){
                            if(channel.equals("test") || channel.equals("test2")){
                                logger.info("Received data from channel " + channel + ". Data - " + data);
                            }
                        }
                    }, channels);
                }
            }.start();
        } catch (Exception e) {
            logger.severe("Failed to subscribe to channels " + ". Error: " + e.getMessage());
        }
    }

    public void publish(String channel,  String data){
        logger.info("Publishing data to channel" + channel + ". Data - " + data);
        try(Jedis publisher = new Jedis("localhost", 6379)){
            publisher.publish(channel, data);
        } catch (Exception e) {
            logger.severe("Failed to publish data to channel " + channel + ". Error: " + e.getMessage());
        }
    }
}
