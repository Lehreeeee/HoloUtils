package me.lehreeeee.HoloUtils.managers;

import com.zaxxer.hikari.HikariConfig;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLManager {
    private static MySQLManager instance;
    private final Logger logger;
    private final HoloUtils plugin;
    private HikariDataSource dataSource;
    private static final String EMPTY_INVENTORY = "rO0ABXcEAAAAAA==";

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
        config.setPoolName("HoloUtils-Connection-Pool");

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP connection pool opened.");
    }

    public void closeConnectionPool(){
        if(dataSource != null){
            dataSource.close();
            logger.info("HikariCP connection pool closed.");
        }
    }

    public void claimOldAccessories(String uuid){
        // Query in async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection con = dataSource.getConnection()){
                String sql = "SELECT * FROM mmoinventory_inventories_rework WHERE uuid = ? AND inventory <> ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,uuid);
                stmt.setString(2,EMPTY_INVENTORY);

                ResultSet result = stmt.executeQuery();

                if(result.next()){
                    String inventoryBase64 = result.getString("inventory");
                    // Back to server main thread
                    Bukkit.getScheduler().runTask(plugin, () -> decodeInventory(inventoryBase64, uuid));
                } else {
                    logger.info("Entry not found or inventory empty, considered claimed for " + uuid);
                }
            } catch (SQLException e) {
                logger.severe("Failed to query from MySQL server." + " Error: " + e.getMessage());
            }
        });
    }

    private void decodeInventory(String inventoryBase64, String uuid){
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(inventoryBase64));
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);

            // Read item count
            int size = bukkitObjectInputStream.readInt();

            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            ItemStack shulker = new ItemStack(Material.ORANGE_SHULKER_BOX,1);
            BlockStateMeta bsm = (BlockStateMeta) shulker.getItemMeta();
            ShulkerBox shulkerBox = (ShulkerBox) bsm.getBlockState();
            Inventory inventory = shulkerBox.getInventory();

            for (int i = 0; i < size; i++) {
                // Skip item slot
                bukkitObjectInputStream.readInt();

                // Read itemstack and dump into shulker
                inventory.addItem((ItemStack) bukkitObjectInputStream.readObject());
            }

            if(player != null){
                bsm.setBlockState(shulkerBox);
                shulker.setItemMeta(bsm);
                HashMap<Integer,ItemStack> extraItems = player.getInventory().addItem(shulker);

                // Inventory full, return and ask to clear inventory
                if(!extraItems.isEmpty()){
                    sendFeedbackMessage(player,"<#FFA500>Your inventory is full, please clear up some space first.");
                    return;
                } else {
                    sendFeedbackMessage(player,"<#FFA500>Successfully claimed old accessories. You should find a orange shulker box in your inventory.");
                }

                // Done giving items to player, update entry from table to prevent 2nd claim
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    updateEntryforClaimedPlayer(uuid);
                });

                // Also remove perm to avoid unneeded query
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"lp user " + player.getName() + " permission unset holoutils.claim_accessories");
            }

            bukkitObjectInputStream.close();
        } catch (Exception e) {
            logger.severe("Failed to decode/deserialize inventory." + " Error: " + e.getMessage());
        }
    }

    private void updateEntryforClaimedPlayer(String uuid){
        try(Connection con = dataSource.getConnection()){
            logger.info("User " + uuid + " claimed their accessories, updating entry.");

            String sql = "UPDATE mmoinventory_inventories_rework SET inventory = ? WHERE uuid = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1,EMPTY_INVENTORY);
            stmt.setString(2,uuid);

            stmt.executeUpdate();
        } catch (SQLException e){
            logger.severe("Failed to update entry for claimed player." + " Error: " + e.getMessage());
        }
    }

    private void sendFeedbackMessage(Player player, String msg){
        logger.info(MessageHelper.getPlainText(msg));

        if(player != null) player.sendMessage(MessageHelper.process(msg,true));
    }
}

