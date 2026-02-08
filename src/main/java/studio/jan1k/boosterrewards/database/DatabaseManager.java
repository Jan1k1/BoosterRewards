package studio.jan1k.boosterrewards.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.Logs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.File;

public class DatabaseManager {

    private final BoosterReward plugin;
    private HikariDataSource dataSource;
    private String tablePrefix;

    public DatabaseManager(BoosterReward plugin) {
        this.plugin = plugin;
        this.tablePrefix = plugin.getConfig().getString("database.table-prefix", "booster_");
        connect();
        initTables();
    }

    private void connect() {
        String type = plugin.getConfig().getString("database.type", "H2").toUpperCase();
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool-size", 10));
        config.setConnectionTimeout(plugin.getConfig().getLong("database.timeout", 5000));
        config.setPoolName("BoosterRewards-Pool");

        if (type.equals("MYSQL") || type.equals("MARIADB")) {
            String host = plugin.getConfig().getString("database.host");
            String port = plugin.getConfig().getString("database.port");
            String dbName = plugin.getConfig().getString("database.database");
            String user = plugin.getConfig().getString("database.username");
            String pass = plugin.getConfig().getString("database.password");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&autoReconnect=true");
            config.setUsername(user);
            config.setPassword(pass);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File dataFolder = new File(plugin.getDataFolder(), "database");
            if (!dataFolder.exists())
                dataFolder.mkdirs();

            config.setJdbcUrl("jdbc:h2:" + dataFolder.getAbsolutePath() + "/BoosterRewards;MODE=MySQL");
            config.setDriverClassName("org.h2.Driver");
        }
        try {
            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            Logs.error("Failed to connect to database!");
            e.printStackTrace();
        }
    }

    private void initTables() {
        String usersTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "discord_id VARCHAR(20) NOT NULL, " +
                "boost_start BIGINT, " +
                "last_reward_claim BIGINT" +
                ");";

        String boostersTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "boosters (" +
                "discord_id VARCHAR(20) PRIMARY KEY, " +
                "uuid VARCHAR(36), " +
                "username VARCHAR(16), " +
                "boost_start BIGINT NOT NULL, " +
                "boost_count INT DEFAULT 1, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "last_checked BIGINT" +
                ");";

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(usersTable)) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(boostersTable)) {
                stmt.execute();
            }

            // Alter table for existing installations
            try (PreparedStatement stmt = conn.prepareStatement(
                    "ALTER TABLE " + tablePrefix + "boosters ADD COLUMN IF NOT EXISTS boost_count INT DEFAULT 1")) {
                stmt.execute();
            } catch (SQLException ignored) {
            }

            try (PreparedStatement stmt = conn
                    .prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "pending_rewards (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "reward_data TEXT NOT NULL, " +
                            "reward_type VARCHAR(32) NOT NULL DEFAULT 'booster', " +
                            "created_at BIGINT NOT NULL)")) {
                stmt.execute();
            }

            try (PreparedStatement stmt = conn
                    .prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "claimed_rewards (" +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "reward_type VARCHAR(32) NOT NULL, " +
                            "claimed_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (uuid, reward_type))")) {
                stmt.execute();
            }
        } catch (SQLException e) {
            Logs.error("Failed to initialize database tables!");
            e.printStackTrace();
        }
    }

    public void addPendingReward(UUID uuid, String rewardData, String rewardType) {
        String sql = "INSERT INTO " + tablePrefix
                + "pending_rewards (uuid, reward_data, reward_type, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rewardData);
            stmt.setString(3, rewardType);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PendingReward> getPendingRewards(UUID uuid) {
        List<PendingReward> rewards = new ArrayList<>();
        String sql = "SELECT id, reward_data, reward_type, created_at FROM " + tablePrefix
                + "pending_rewards WHERE uuid = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rewards.add(new PendingReward(
                            rs.getInt("id"),
                            uuid,
                            rs.getString("reward_data"),
                            rs.getString("reward_type"),
                            rs.getLong("created_at")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rewards;
    }

    public boolean hasPendingReward(UUID uuid, String rewardType) {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "pending_rewards WHERE uuid = ? AND reward_type = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rewardType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removePendingReward(int id) {
        String sql = "DELETE FROM " + tablePrefix + "pending_rewards WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasAlreadyClaimed(UUID uuid, String rewardType) {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "claimed_rewards WHERE uuid = ? AND reward_type = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rewardType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addClaimRecord(UUID uuid, String rewardType) {
        String sql = "INSERT INTO " + tablePrefix + "claimed_rewards (uuid, reward_type, claimed_at) VALUES (?, ?, ?)";
        if (plugin.getConfig().getString("database.type", "H2").equalsIgnoreCase("H2")) {
            sql = "MERGE INTO " + tablePrefix
                    + "claimed_rewards (uuid, reward_type, claimed_at) KEY (uuid, reward_type) VALUES (?, ?, ?)";
        }
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rewardType);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class PendingReward {
        private final int id;
        private final UUID uuid;
        private final String rewardData;
        private final String rewardType;
        private final long createdAt;

        public PendingReward(int id, UUID uuid, String rewardData, String rewardType, long createdAt) {
            this.id = id;
            this.uuid = uuid;
            this.rewardData = rewardData;
            this.rewardType = rewardType;
            this.createdAt = createdAt;
        }

        public int getId() {
            return id;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getRewardData() {
            return rewardData;
        }

        public String getRewardType() {
            return rewardType;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    public void removeClaimRecord(UUID uuid, String rewardType) {
        String sql = "DELETE FROM " + tablePrefix + "claimed_rewards WHERE uuid = ? AND reward_type = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rewardType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(UUID uuid, String discordId) {
        String sql = "INSERT INTO " + tablePrefix
                + "users (uuid, discord_id, boost_start, last_reward_claim) VALUES (?, ?, 0, 0) "
                + "ON DUPLICATE KEY UPDATE discord_id = VALUES(discord_id)";
        if (plugin.getConfig().getString("database.type", "H2").equalsIgnoreCase("H2")) {
            sql = "MERGE INTO " + tablePrefix
                    + "users (uuid, discord_id, boost_start, last_reward_claim) KEY (uuid) VALUES (?, ?, 0, 0)";
        }

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, discordId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBooster(String discordId, UUID uuid, String username, long boostStart, int boostCount) {
        String sql = "INSERT INTO " + tablePrefix
                + "boosters (discord_id, uuid, username, boost_start, boost_count, is_active, last_checked) "
                + "VALUES (?, ?, ?, ?, ?, TRUE, ?) "
                + "ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), username = VALUES(username), "
                + "boost_start = VALUES(boost_start), boost_count = VALUES(boost_count), is_active = TRUE, last_checked = VALUES(last_checked)";
        if (plugin.getConfig().getString("database.type", "H2").equalsIgnoreCase("H2")) {
            sql = "MERGE INTO " + tablePrefix
                    + "boosters (discord_id, uuid, username, boost_start, boost_count, is_active, last_checked) "
                    + "KEY (discord_id) VALUES (?, ?, ?, ?, ?, TRUE, ?)";
        }

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            stmt.setString(2, uuid != null ? uuid.toString() : null);
            stmt.setString(3, username);
            stmt.setLong(4, boostStart);
            stmt.setInt(5, boostCount);
            stmt.setLong(6, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setBoosterInactive(String discordId) {
        String sql = "UPDATE " + tablePrefix + "boosters SET is_active = FALSE, last_checked = ? WHERE discord_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, discordId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setBoosterStatus(UUID uuid, boolean isBooster, int boostCount) {
        String discordId = getDiscordId(uuid);
        if (discordId == null)
            return;

        if (isBooster) {
            String sql = "INSERT INTO " + tablePrefix
                    + "boosters (discord_id, uuid, boost_count, is_active, last_checked, boost_start, username) "
                    + "VALUES (?, ?, ?, TRUE, ?, ?, 'Unknown') "
                    + "ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), boost_count = VALUES(boost_count), is_active = TRUE, last_checked = VALUES(last_checked)";

            if (plugin.getConfig().getString("database.type", "H2").equalsIgnoreCase("H2")) {
                sql = "MERGE INTO " + tablePrefix
                        + "boosters (discord_id, uuid, boost_count, is_active, last_checked, boost_start, username) "
                        + "KEY (discord_id) VALUES (?, ?, ?, TRUE, ?, ?, 'Unknown')";
            }

            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, discordId);
                stmt.setString(2, uuid.toString());
                stmt.setInt(3, boostCount);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.setLong(5, System.currentTimeMillis()); // Use current time as boost_start if new
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            setBoosterInactive(discordId);
        }
    }

    public List<String> getAllActiveBoosters() {
        List<String> boosters = new ArrayList<>();
        String sql = "SELECT discord_id FROM " + tablePrefix + "boosters WHERE is_active = TRUE";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                boosters.add(rs.getString("discord_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boosters;
    }

    public int getActiveBoosterCount() {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "boosters WHERE is_active = TRUE";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getDiscordId(UUID uuid) {
        String sql = "SELECT discord_id FROM " + tablePrefix + "users WHERE uuid = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("discord_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UUID getUuid(String discordId) {
        String sql = "SELECT uuid FROM " + tablePrefix + "users WHERE discord_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    return uuidStr != null ? UUID.fromString(uuidStr) : null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeUser(UUID uuid) {
        String sql = "DELETE FROM " + tablePrefix + "users WHERE uuid = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBoostCount(UUID uuid) {
        String discordId = getDiscordId(uuid);
        if (discordId == null)
            return 0;

        String sql = "SELECT boost_count FROM " + tablePrefix + "boosters WHERE discord_id = ? AND is_active = TRUE";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("boost_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isBoosting(UUID uuid) {
        String discordId = getDiscordId(uuid);
        if (discordId == null)
            return false;

        String sql = "SELECT is_active FROM " + tablePrefix + "boosters WHERE discord_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getBoolean("is_active");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
