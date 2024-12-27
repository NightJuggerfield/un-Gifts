package dev.ungifts.data;
import dev.ungifts.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
public class YAMLStorage implements DataStorage {
    private final Main plugin;
    private final File usersFile;
    private FileConfiguration usersConfig;
    public YAMLStorage(Main plugin) {
        this.plugin = plugin;
        usersFile = new File(plugin.getDataFolder(), "users.yml");
        load();
    }
    private void load() {
        if (!usersFile.exists()) {
            try {
                usersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать файл users.yml!", e);
            }
        }
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
    }
    private void save() {
        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить файл users.yml!", e);
        }
    }
    @Override
    public long getLastClaimTime(UUID uuid, String type) {
        String path = "players." + uuid.toString() + "." + type;
        return usersConfig.getLong(path, 0);
    }
    @Override
    public void setLastClaimTime(UUID uuid, String type, long time) {
        String path = "players." + uuid.toString() + "." + type;
        usersConfig.set(path, time);
        save();
    }
    @Override
    public void close() {
        save();
    }
    @Override
    public Map<String, Long> getPlayerData(UUID uuid) {
        Map<String, Long> data = new HashMap<>();
        String path = "players." + uuid.toString();
        if(usersConfig.getConfigurationSection(path) == null) {
            return data;
        }
        for(String key : usersConfig.getConfigurationSection(path).getKeys(false)) {
            data.put(key, usersConfig.getLong(path + "." + key, 0));
        }
        return data;
    }
    @Override
    public void setPlayerData(UUID uuid, Map<String, Long> data) {
        String path = "players." + uuid.toString();
        for(Map.Entry<String, Long> entry : data.entrySet()) {
            usersConfig.set(path + "." + entry.getKey(), entry.getValue());
        }
        save();
    }
}