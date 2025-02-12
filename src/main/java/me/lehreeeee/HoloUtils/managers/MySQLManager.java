package me.lehreeeee.HoloUtils.managers;

import com.zaxxer.hikari.HikariConfig;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLManager {
    private static MySQLManager instance;
    private final Logger logger;
    private final HoloUtils plugin;
    private HikariDataSource dataSource;

    private MySQLManager(HoloUtils plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static MySQLManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("MySQLManager not initialized.");
        }
        return instance;
    }

    public static void initialize(HoloUtils plugin) {
        if(instance == null){
            instance = new MySQLManager(plugin);
        }
    }

    public void loadMySQLConfig(ConfigurationSection MySQLConfig) {
        // Read from config.yml
        String url = "jdbc:mysql://" + MySQLConfig.getString("host","localhost")
                + ":" + MySQLConfig.getInt("port", 3306) + "/" + MySQLConfig.getString("database","") + "?useSSL=false&serverTimezone=UTC";
        String user = MySQLConfig.getString("username","");
        String password = MySQLConfig.getString("password","");

        // Set hikari config
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP connection pool opened.");
    }

    public void closeConnectionPool(){
        if(dataSource != null){
            dataSource.close();
            logger.info("HikariCP connection pool closed.");
        }
    }

    public void query(String uuid){
        // Query in async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection con = dataSource.getConnection()){
                String sql = "SELECT * FROM mmoinventory_inventories WHERE uuid = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,uuid);

                ResultSet result = stmt.executeQuery();

                if(result.next()){
                    String inventoryBase64 = result.getString("inventory");
                    logger.info("Encoded inventory: " + inventoryBase64);

                    // Back to server main thread
                    Bukkit.getScheduler().runTask(plugin, () -> decodeInventory(inventoryBase64, uuid));
                }
            } catch (SQLException e) {
                logger.severe("Failed to query from MySQL server." + " Error: " + e.getMessage());
            }
        });
    }

    public void decodeInventory(String inventoryBase64, String uuid){
        try{
            byte[] inventoryBytes = Base64.getDecoder().decode(inventoryBase64);
            logger.info("Decoded inventory: " + new String(inventoryBytes));

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inventoryBytes);
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);

            // Read item count
            int size = bukkitObjectInputStream.readInt();
            logger.info("Item count: " + size);

            ItemStack shulker = new ItemStack(Material.ORANGE_SHULKER_BOX,1);
            BlockStateMeta bsm = (BlockStateMeta) shulker.getItemMeta();

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

