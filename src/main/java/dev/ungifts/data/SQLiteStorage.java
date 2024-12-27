package dev.ungifts.data;
import dev.ungifts.Main;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
public class SQLiteStorage implements DataStorage {
    private final Main plugin;
    private Connection connection;
    private File dbFile;
    public SQLiteStorage(Main plugin) {
        this.plugin = plugin;
        dbFile = new File(plugin.getDataFolder(), "users.db");
        setupDatabase();
    }
    private void setupDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка подключения к SQLite!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private void createTable() {
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS gifts_data (uuid VARCHAR(36) NOT NULL PRIMARY KEY, data TEXT)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании таблицы SQLite!", e);
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
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при закрытии подключения к SQLite!", e);
            }
        }
    }
    @Override
    public Map<String, Long> getPlayerData(UUID uuid) {
        String query = "SELECT data FROM gifts_data WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String data = resultSet.getString("data");
                return parseData(data);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке данных игрока из SQLite!", e);
        }
        return null;
    }
    @Override
    public void setPlayerData(UUID uuid, Map<String, Long> data) {
        String query = "INSERT OR REPLACE INTO gifts_data (uuid, data) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, formatData(data));
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при сохранении данных игрока в SQLite!", e);
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