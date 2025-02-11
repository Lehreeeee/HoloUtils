package me.lehreeeee.HoloUtils.managers;

import me.lehreeeee.HoloUtils.utils.MessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MySQLManager {
    private static MySQLManager instance;
    private final Logger logger;
    private String url;
    private String user;
    private String password;
    private Connection connection;

    private MySQLManager(Logger logger){
        this.logger = logger;
    }

    public static MySQLManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("MySQLManager not initialized.");
        }
        return instance;
    }

    public static void initialize(Logger logger) {
        if(instance == null){
            instance = new MySQLManager(logger);
        }
    }

    public void loadMySQLConfig(ConfigurationSection MySQLConfig) {
        this.url = "jdbc:mysql://" + MySQLConfig.getString("host","localhost")
                + ":" + MySQLConfig.getInt("port", 3306) + "/" + MySQLConfig.getString("database","") + "?useSSL=false&serverTimezone=UTC";
        this.user = MySQLConfig.getString("username","");
        this.password = MySQLConfig.getString("password","");
    }

    public boolean connect() {
        try{
            if(connection != null && !connection.isClosed()){
                return true;
            }
            connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch(SQLException e){
            logger.severe("Failed to connect to MySQL server." + " Error: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try{
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
        } catch(SQLException e){
            logger.severe("Failed to disconnect from MySQL server." + " Error: " + e.getMessage());
        }
    }

    public void query(String uuid){
        try{
            String sql = "SELECT * FROM mmoinventory_inventories WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,uuid);

            ResultSet result = stmt.executeQuery();

            if(result.next()){
                String inventoryBase64 = result.getString("inventory");
                logger.info("Encoded inventory: " + inventoryBase64);

                decodeInventory(inventoryBase64);
            }
        } catch(SQLException e){
            logger.severe("Failed to query from MySQL server." + " Error: " + e.getMessage());
        }
    }

    public void decodeInventory(String inventoryBase64){
        try{
            byte[] inventoryBytes = Base64.getDecoder().decode(inventoryBase64);
            logger.info("Decoded inventory: " + new String(inventoryBytes));

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inventoryBytes);
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);

            // Read item count
            int size = bukkitObjectInputStream.readInt();
            logger.info("Item count: " + size);

            Map<Integer, ItemStack> inventoryMap = new HashMap<>();
            for (int i = 0; i < size; i++) {
                // Read item slot
                int slot = bukkitObjectInputStream.readInt();
                logger.info("Reading item: " + i);
                logger.info("Item slot: " + slot);

                // Read itemstack
                ItemStack itemStack = (ItemStack) bukkitObjectInputStream.readObject();
                ItemMeta itemMeta = itemStack.getItemMeta();
                logger.info("Item display name: " + MessageHelper.getPlainText(MessageHelper.revert(itemMeta.displayName())));
            }
            bukkitObjectInputStream.close();
        } catch (Exception e) {
            logger.severe("Failed to decode/deserialize inventory." + " Error: " + e.getMessage());
        }
    }
}

