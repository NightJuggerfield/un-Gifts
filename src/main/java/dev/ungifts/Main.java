package dev.ungifts;
import dev.ungifts.commands.GiftsCommand;
import dev.ungifts.data.DataStorage;
import dev.ungifts.data.MySQLStorage;
import dev.ungifts.data.SQLiteStorage;
import dev.ungifts.data.YAMLStorage;
import dev.ungifts.gifts.GiftManager;
import dev.ungifts.menu.MenuListener;
import org.bukkit.plugin.java.JavaPlugin;
import dev.ungifts.placeholders.PlaceholderExp;
public class Main extends JavaPlugin {
    private ConfigManager configManager;
    private DataStorage dataStorage;
    private GiftManager giftManager;
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        loadDataStorage();
        giftManager = new GiftManager(this);
        new GiftsCommand(this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderExp(this).register();
        } else {
            getLogger().warning("PlaceholderAPI не найден, плейсхолдеры работать не будут.");
        }
    }
    @Override
    public void onDisable() {
        if (dataStorage != null) {
            dataStorage.close();
        }
    }
    private void loadDataStorage() {
        String saveType = configManager.getConfig().getString("gifts.save-type", "db").toLowerCase();
        switch (saveType) {
            case "mysql":
                dataStorage = new MySQLStorage(this);
                break;
            case "yaml":
                dataStorage = new YAMLStorage(this);
                break;
            case "db":
            default:
                dataStorage = new SQLiteStorage(this);
                break;
        }
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public DataStorage getDataStorage() {
        return dataStorage;
    }
    public GiftManager getGiftManager() {
        return giftManager;
    }
}