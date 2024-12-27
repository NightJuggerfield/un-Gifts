package dev.ungifts.gifts;
import dev.ungifts.Main;
import dev.ungifts.menu.Menu;
import dev.ungifts.menu.MenuItem;
import dev.ungifts.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import java.util.List;
public class GiftManager {
    private final Main plugin;
    private Menu menu;
    private final Map<String, Gift> gifts = new HashMap<>();
    public GiftManager(Main plugin) {
        this.plugin = plugin;
        loadGifts();
        loadMenu();
    }
    public Map<String, Gift> getGifts() {
        return gifts;
    }
    private void loadGifts() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        ConfigurationSection itemsSection = config.getConfigurationSection("gifts.menu.items");
        if (itemsSection == null) {
            plugin.getLogger().log(Level.WARNING, "Раздел 'gifts.menu.items' не найден в конфигурации.");
            return;
        }
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if(itemSection == null) continue;
            String typeString = itemSection.getString("type");
            if (typeString == null || typeString.equalsIgnoreCase("AIR")) continue;
            try {
                GiftType type = GiftType.valueOf(typeString.toUpperCase());
                Material material = Material.valueOf(itemSection.getString("id", "STONE").toUpperCase());
                String name = itemSection.getString("name");
                List<String> lore = itemSection.getStringList("lore");
                String nbt = itemSection.getStringList("nbt").stream().findFirst().orElse("");
                List<String> commands = config.getStringList("gifts." + type.toString().toLowerCase() + ".commands");
                List<String> delayMessage = config.getStringList("gifts." + type.toString().toLowerCase() + ".delay in receiving");
                Gift gift = new Gift(type, material, name, lore, commands, delayMessage, nbt);
                gifts.put(key, gift);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Неверно указанный тип предмета " + typeString + " или ID материала в config.yml: " + e.getMessage());
            }
        }
    }
    private void loadMenu() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String menuName = config.getString("gifts.menu.name", "Награды");
        List<String> overview = config.getStringList("gifts.menu.overview");
        if (overview.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Неверно указан overview в config.yml");
            menu = null;
            return;
        }
        List<MenuItem> menuItems = new ArrayList<>();
        for (int row = 0; row < overview.size(); row++) {
            String line = overview.get(row);
            for (int col = 0; col < line.length(); col++) {
                char key = line.charAt(col);
                String itemKey = String.valueOf(key);
                Gift gift = gifts.get(itemKey);
                if(gift != null) {
                    menuItems.add(new MenuItem(row * 9 + col, gift.getItemStack(), player -> handleGiftClick(player, gift)));
                } else {
                    Material material = Material.AIR;
                    menuItems.add(new MenuItem(row * 9 + col, new ItemStack(material), player -> {}));
                }
            }
        }
        menu = new Menu(menuName, menuItems);
    }
    public void handleGiftClick(Player player, Gift gift) {
        long delay = gift.getDelay(player.getUniqueId(), plugin);
        if (delay <= 0) {
            List<String> commands = gift.getCommands();
            if(commands != null && !commands.isEmpty()) {
                for(String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%p", player.getName()));
                }
                plugin.getDataStorage().setLastClaimTime(player.getUniqueId(), gift.getType().toString(), System.currentTimeMillis());
            }
            player.closeInventory();
        } else {
            sendDelayMessage(player, gift, delay);
        }
    }
    private void sendDelayMessage(Player player, Gift gift, long delay) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(gift, "Gift cannot be null");
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(plugin.getConfigManager(), "ConfigManager cannot be null");
        Objects.requireNonNull(plugin.getConfigManager().getConfig(), "Config cannot be null");

        String time = TimeUtil.formatTime(delay, plugin.getConfigManager().getConfig());
        if (time == null) {
            plugin.getLogger().warning("TimeUtil.formatTime returned null");
            return;
        }

        String giftType = gift.getType().toString().toLowerCase();
        if (giftType == null || giftType.isEmpty()) {
            plugin.getLogger().warning("Gift type is null or empty");
            return;
        }

        String basePath = "gifts." + giftType + ".delay in receiving";
        ConfigurationSection section = plugin.getConfigManager().getConfig().getConfigurationSection(basePath);
        if (section == null) {
            plugin.getLogger().warning("Configuration section for " + basePath + " is null");
            return;
        }

        ConfigurationSection chatSection = section.getConfigurationSection("chat");
        if (chatSection != null && chatSection.getBoolean("enabled", false)) {
            List<String> messages = chatSection.getStringList("message");
            if (messages != null && !messages.isEmpty()) {
                for (String message : messages) {
                    player.sendMessage(message.replace("%" + giftType + "-delay%", time));
                }
            }
        }

        ConfigurationSection titleSection = section.getConfigurationSection("title");
        if (titleSection != null && titleSection.getBoolean("enabled", false)) {
            String mainTitle = titleSection.getString("main title");
            String subTitle = titleSection.getString("sub title");
            if (mainTitle != null && subTitle != null) {
                player.sendTitle(
                        mainTitle.replace("%" + giftType + "-delay%", time),
                        subTitle.replace("%" + giftType + "-delay%", time),
                        10, 70, 20
                );
            }
        }

        ConfigurationSection soundSection = section.getConfigurationSection("sound");
        if (soundSection != null && soundSection.getBoolean("enabled", false)) {
            String soundString = soundSection.getString("sound");
            if (soundString != null) {
                try {
                    Sound sound = Sound.valueOf(soundString);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный звук: " + soundString);
                }
            }
        }

        ConfigurationSection hotbarSection = section.getConfigurationSection("hotbar");
        if (hotbarSection != null && hotbarSection.getBoolean("enabled", false)) {
            String hotbarMessage = hotbarSection.getString("message");
            if (hotbarMessage != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(hotbarMessage.replace("%" + giftType + "-delay%", time)));
            }
        }
    }
    public Menu getMenu() {
        return menu;
    }
}