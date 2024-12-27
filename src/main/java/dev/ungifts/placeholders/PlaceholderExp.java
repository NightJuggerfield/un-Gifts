package dev.ungifts.placeholders;
import dev.ungifts.Main;
import dev.ungifts.gifts.Gift;
import dev.ungifts.gifts.GiftType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.UUID;
public class PlaceholderExp extends PlaceholderExpansion {
    private final Main plugin;
    public PlaceholderExp(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "newgifts";
    }
    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        UUID uuid = player.getUniqueId();
        if (params.equalsIgnoreCase("rewardsf")) {
            int availableRewards = getAvailableRewardsCount(uuid);
            return availableRewards + " " + getRewardText(availableRewards);
        } else if (params.equalsIgnoreCase("rewards")) {
            return String.valueOf(getAvailableRewardsCount(uuid));
        } else if (params.equalsIgnoreCase("daily")) {
            return String.valueOf(isGiftAvailable(uuid, GiftType.DAILY));
        } else if (params.equalsIgnoreCase("weekly")) {
            return String.valueOf(isGiftAvailable(uuid, GiftType.WEEKLY));
        } else if (params.equalsIgnoreCase("monthly")) {
            return String.valueOf(isGiftAvailable(uuid, GiftType.MONTHLY));
        }
        return "";
    }
    private Map<String, Gift> getGiftsSafe() {
        if (plugin.getGiftManager() != null) {
            return plugin.getGiftManager().getGifts();
        }
        return null;
    }
    private int getAvailableRewardsCount(UUID uuid) {
        Map<String, Gift> gifts = getGiftsSafe();
        if (gifts == null) {
            return 0;
        }
        int count = 0;
        for (Gift gift : gifts.values()) {
            if (gift.getDelay(uuid, plugin) <= 0) {
                count++;
            }
        }
        return count;
    }
    private String getRewardText(int count) {
        if (count == 1) {
            return "награда";
        } else if (count > 1 && count < 5) {
            return "награды";
        } else {
            return "наград";
        }
    }
    private boolean isGiftAvailable(UUID uuid, GiftType type) {
        Map<String, Gift> gifts = getGiftsSafe();
        if (gifts == null) {
            return false;
        }
        for (Gift gift : gifts.values()) {
            if (gift.getType() == type && gift.getDelay(uuid, plugin) <= 0) {
                return true;
            }
        }
        return false;
    }
}