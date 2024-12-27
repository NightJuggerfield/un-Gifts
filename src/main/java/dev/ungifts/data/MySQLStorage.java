package dev.ungifts.data;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.ungifts.Main;
import org.bukkit.configuration.ConfigurationSection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
public class MySQLStorage implements DataStorage {
    private final Main plugin;
    private HikariDataSource dataSource;
    public MySQLStorage(Main plugin) {
        this.plugin = plugin;
        setupDatabase();
    }
    private void setupDatabase() {
        ConfigurationSection mysqlConfig = plugin.getConfigManager().getConfig().getConfigurationSection("mysql");
        if(mysqlConfig == null) {
            plugin.getLogger().log(Level.SEVERE, "Не настроен MySQL в config.yml");
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + mysqlConfig.getString("host", "localhost") + ":" +
                mysqlConfig.getInt("port", 3306) + "/" + mysqlConfig.getString("database", "newgifts"));
        config.setUsername(mysqlConfig.getString("username", "user"));
        config.setPassword(mysqlConfig.getString("password", "password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.setMaximumPoolSize(10);
        try {
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка подключения к MySQL!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        createTable();
    }
    private void createTable() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS gifts_data (uuid VARCHAR(36) NOT NULL PRIMARY KEY, data TEXT)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании таблицы MySQL!", e);
        }
    }
    @Override
    public long getLastClaimTime(UUID uuid, String type) {
        Map<String, Long> playerData = getPlayerData(uuid);
        if (playerData == null || !playerData.containsKey(type)) {
            return 0;
        }
        return playerData.get(type);
    }
    @Override
    public void setLastClaimTime(UUID uuid, String type, long time) {
        Map<String, Long> playerData = getPlayerData(uuid);
        if (playerData == null) {
            playerData = new HashMap<>();
        }
        playerData.put(type, time);
        setPlayerData(uuid, playerData);
    }
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    @Override
    public Map<String, Long> getPlayerData(UUID uuid) {
        String query = "SELECT data FROM gifts_data WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String data = resultSet.getString("data");
                return parseData(data);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке данных игрока из MySQL!", e);
        }
        return null;
    }
    @Override
    public void setPlayerData(UUID uuid, Map<String, Long> data) {
        String query = "INSERT INTO gifts_data (uuid, data) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE data = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, formatData(data));
            statement.setString(3, formatData(data));
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при сохранении данных игрока в MySQL!", e);
        }
    }
    private String formatData(Map<String, Long> data) {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Long> entry : data.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    private Map<String, Long> parseData(String data) {
        Map<String, Long> result = new HashMap<>();
        if (data == null || data.isEmpty()) {
            return result;
        }
        String[] entries = data.split(",");
        for (String entry : entries) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    result.put(parts[0], Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    plugin.getLogger().log(Level.WARNING, "Не удалось спарсить значение " + parts[1], e);
                }
            }
        }
        return result;
    }
}