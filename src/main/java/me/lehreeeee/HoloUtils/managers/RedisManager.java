package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.utils.LoggerUtil;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.text.MessageFormat;
import java.util.Arrays;

public class RedisManager {
    private static RedisManager instance;
    private String redisHost = "localhost";
    private Integer redisPort = 6379;
    private String redisUserName, redisPassword = "";

    public static RedisManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("RedisManager not initialized.");
        }
        return instance;
    }

    public static void initialize() {
        if(instance == null){
            instance = new RedisManager();
        }
    }

    public void loadRedisConfig(ConfigurationSection redisConfig) {
        redisHost = redisConfig != null ? redisConfig.getString("host", "localhost") : "localhost";
        redisPort = redisConfig != null ? redisConfig.getInt("port", 6379) : 6379;

        if(redisConfig == null){
            LoggerUtil.info("Redis config section not found, using default - localhost:6379");
            return;
        }

        redisUserName = redisConfig.getString("username", "");
        redisPassword = redisConfig.getString("password", "");

        LoggerUtil.info(MessageFormat.format("Loaded Redis config, will be connecting to {0}:{1}",redisHost,redisPort.toString()));
    }

    public void subscribe(){
        try(Jedis subscriber = new Jedis(redisHost, redisPort)){
            if(redisPassword != null && !redisPassword.isEmpty()){
                LoggerUtil.info("Found redis password, performing authentication.");
                subscriber.auth(redisUserName,redisPassword);
            }

            subscriber.connect();

            String[] channels = {"holo-test", "holo-devchat"};

            new Thread("Holo Redis Subscriber Thread"){
                @Override
                public void run(){

                    LoggerUtil.info("Started subscriber thread - " + this.getName());
                    LoggerUtil.info("Subscribing to channels " + Arrays.toString(channels));
                    subscriber.subscribe(new JedisPubSub(){
                        @Override
                        public void onMessage(String channel, String data){
                            handleMessage(channel,data);
                        }
                    }, channels);
                }
            }.start();
        } catch (Exception e) {
            LoggerUtil.severe("Failed to subscribe to channels" + ". Error: " + e.getMessage());
        }
    }

    public void publish(String channel,  String data){
        //logger.info("Publishing data to channel " + channel + ". Data - " + data);
        try(Jedis publisher = new Jedis("localhost", 6379)){
            publisher.publish(channel, data);
        } catch (Exception e) {
            LoggerUtil.severe("Failed to publish data to channel " + channel + ". Error: " + e.getMessage());
        }
    }

    private void handleMessage(String channel, String data){
        if(channel.equals("holo-test")){
            LoggerUtil.info("Received data from test channel " + channel + ". Data - " + data);
        }
        if(channel.equals("holo-devchat")){
            //logger.info("Received devchat data - " + data);
            DevChatManager.getInstance().sendMessage(data);
        }
    }
}
