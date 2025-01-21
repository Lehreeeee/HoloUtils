package me.lehreeeee.HoloUtils.managers;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.logging.Logger;

public class RedisManager {
    private static RedisManager instance;
    private final Logger logger;

    private RedisManager(Logger logger){
        this.logger = logger;
    }

    public static RedisManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("RedisManager not initialized.");
        }
        return instance;
    }

    public static void initialize(Logger logger) {
        if (instance == null) {
            instance = new RedisManager(logger);
        }
    }

    public void subscribe(){
        try(Jedis subscriber = new Jedis("localhost", 6379)){
            subscriber.connect();

            String[] channels = {"holo-test", "holo-devchat"};

            new Thread("Holo Redis Subscriber Thread"){
                @Override
                public void run(){

                    logger.info("Started subscriber thread - " + this.getName());
                    logger.info("Subscribing to channels " + Arrays.toString(channels));
                    subscriber.subscribe(new JedisPubSub(){
                        @Override
                        public void onMessage(String channel, String data){
                            handleMessage(channel,data);
                        }
                    }, channels);
                }
            }.start();
        } catch (Exception e) {
            logger.severe("Failed to subscribe to channels " + ". Error: " + e.getMessage());
        }
    }

    public void publish(String channel,  String data){
        //logger.info("Publishing data to channel " + channel + ". Data - " + data);
        try(Jedis publisher = new Jedis("localhost", 6379)){
            publisher.publish(channel, data);
        } catch (Exception e) {
            logger.severe("Failed to publish data to channel " + channel + ". Error: " + e.getMessage());
        }
    }

    private void handleMessage(String channel, String data){
        if(channel.equals("holo-test")){
            logger.info("Received data from test channel " + channel + ". Data - " + data);
        }
        if(channel.equals("holo-devchat")){
            //logger.info("Received devchat data - " + data);
            DevChatManager.getInstance().sendMessage(data);
        }
    }
}
