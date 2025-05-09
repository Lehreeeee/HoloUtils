package me.lehreeeee.HoloUtils.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lehreeeee.HoloUtils.HoloUtils;
import me.lehreeeee.HoloUtils.utils.LoggerUtils;
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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MySQLManager {
    private static MySQLManager instance;
    private final HoloUtils plugin;
    private HikariDataSource dataSource;
    private static final String EMPTY_INVENTORY = "rO0ABXcEAAAAAA==";

    private MySQLManager(HoloUtils plugin){
        this.plugin = plugin;
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
        if(dataSource != null) return;
        if(MySQLConfig == null) LoggerUtils.warning("Unable to find MySQl config section, will be using default values.");

        // Read from config.yml
        String host = MySQLConfig.getString("host", "localhost");
        int port = MySQLConfig.getInt("port", 3306);
        String database = MySQLConfig.getString("database", null);
        String user = MySQLConfig.getString("username", null);
        String password = MySQLConfig.getString("password", null);

        if (user == null || password == null) {
            throw new IllegalArgumentException("Database username and password must be provided.");
        }

        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException("Database name is required.");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";

        // Set hikari config
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("HoloUtils-Connection-Pool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);

        dataSource = new HikariDataSource(config);
        LoggerUtils.info("HikariCP connection pool opened.");

        checkTables();
    }

    public void closeConnectionPool(){
        if(dataSource != null){
            dataSource.close();
            LoggerUtils.info("HikariCP connection pool closed.");
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
                    sendFeedbackMessage(Bukkit.getPlayer(UUID.fromString(uuid)),"<#FFA500>You have no unclaimed accessories.");
                }
            } catch (SQLException e) {
                LoggerUtils.severe("Failed to query from MySQL server." + " Error: " + e.getMessage());
            }
        });
    }

    public void giveEventReward(String uuid, String rewardId, String server){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection con = dataSource.getConnection()){
                String sql = "INSERT INTO holoutils_event_rewards (uuid, rewardid, timegiven, timeclaimed, server_name) "
                        + "VALUES (?, ?, NOW(), NULL, ?)";

                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,uuid);
                stmt.setString(2,rewardId);
                stmt.setString(3,server);

                stmt.executeUpdate();

            } catch (SQLException e) {
                LoggerUtils.severe("Failed to give event rewards." + " Error: " + e.getMessage());
            }
        });
    }

    public void getEventRewards(String uuid, int page, String server, Consumer<List<String>> callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> rewards = new ArrayList<>();
            int limit = 28;
            int offset = (page - 1) * 28;

            try(Connection con = dataSource.getConnection()) {
                String sql = "SELECT id, rewardid, timegiven FROM holoutils_event_rewards WHERE uuid = ? AND server_name = ? AND timeclaimed IS NULL " +
                        "LIMIT ? OFFSET ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,uuid);
                stmt.setString(2,server);
                stmt.setInt(3, limit);
                stmt.setInt(4, offset);

                ResultSet result = stmt.executeQuery();

                while(result.next()){
                    rewards.add(result.getString("rewardid") + ";" +  result.getString("timegiven") + ";" + result.getString("id"));
                }

            } catch (SQLException e){
                LoggerUtils.severe("Failed to get event rewards." + " Error: " + e.getMessage());
            }

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(rewards));
        });
    }

    public void getAllEventRewards(String uuid, String server, Consumer<List<String>> callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> rewards = new ArrayList<>();

            try(Connection con = dataSource.getConnection()) {
                String sql = "SELECT id, rewardid FROM holoutils_event_rewards WHERE uuid = ? AND server_name = ? AND timeclaimed IS NULL";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,uuid);
                stmt.setString(2,server);

                ResultSet result = stmt.executeQuery();

                while(result.next()){
                    rewards.add(result.getString("rewardid") + ";" + result.getString("id"));
                }

            } catch (SQLException e){
                LoggerUtils.severe("Failed to get event rewards." + " Error: " + e.getMessage());
            }

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(rewards));
        });
    }

    public void claimEventRewards(String rowId){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection con = dataSource.getConnection()){
                String sql = "UPDATE holoutils_event_rewards SET timeclaimed = NOW() WHERE id = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1,rowId);

                stmt.executeUpdate();
            } catch (SQLException e){
                LoggerUtils.severe("Failed to update entry for reward claiming for row: " + rowId + ". Error: " + e.getMessage());
            }
        });
    }

    public void claimAllEventRewards(Set<String> rowIds){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try(Connection con = dataSource.getConnection()){
                String placeholders = rowIds.stream()
                        .map(id -> "?")
                        .collect(Collectors.joining(","));

                String sql = "UPDATE holoutils_event_rewards SET timeclaimed = NOW() WHERE id in (" + placeholders + ")";
                PreparedStatement stmt = con.prepareStatement(sql);

                int index = 1;
                for(String id : rowIds){
                    stmt.setInt(index++, Integer.parseInt(id));
                }

                stmt.executeUpdate();
            } catch (SQLException e){
                LoggerUtils.severe("Failed to update entry for reward claiming for rows: " + String.join(",", rowIds) + ". Error: " + e.getMessage());
            }
        });
    }

    private void checkTables(){
        try(Connection con = dataSource.getConnection()){
            Statement stmt = con.createStatement();

            // Create event table
            String table = "holoutils_event_rewards";
            String createTableQuery = "CREATE TABLE IF NOT EXISTS holoutils_event_rewards ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "uuid CHAR(36) NOT NULL, "
                    + "rewardid VARCHAR(255) NOT NULL, "
                    + "timegiven TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "timeclaimed TIMESTAMP NULL, "
                    + "server_name VARCHAR(255) NOT NULL"
                    + ")";

            stmt.executeUpdate(createTableQuery);

            LoggerUtils.info("Ensured table '" + table + "' exists.");

        } catch (SQLException e) {
            LoggerUtils.severe("Failed to check database and tables. Error: " + e.getMessage());
            throw new RuntimeException("Database or table check failed.", e);
        }
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
            }

            bukkitObjectInputStream.close();
        } catch (Exception e) {
            LoggerUtils.severe("Failed to decode/deserialize inventory." + " Error: " + e.getMessage());
        }
    }

    private void updateEntryforClaimedPlayer(String uuid){
        try(Connection con = dataSource.getConnection()){
            LoggerUtils.info("User " + uuid + " claimed their accessories, updating entry.");

            String sql = "UPDATE mmoinventory_inventories_rework SET inventory = ? WHERE uuid = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1,EMPTY_INVENTORY);
            stmt.setString(2,uuid);

            stmt.executeUpdate();
        } catch (SQLException e){
            LoggerUtils.severe("Failed to update entry for claimed player." + " Error: " + e.getMessage());
        }
    }

    private void sendFeedbackMessage(Player player, String msg){
        LoggerUtils.info(MessageHelper.getPlainText(msg));

        if(player != null) player.sendMessage(MessageHelper.process(msg,true));
    }
}

