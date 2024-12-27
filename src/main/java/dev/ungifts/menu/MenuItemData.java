package dev.ungifts.menu;
import dev.ungifts.util.RGBUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.stream.Collectors;
public class MenuItemData {
    private final ItemStack itemStack;
    private List<String> lore;
    private String displayName;
    public MenuItemData(ItemStack itemStack, List<String> lore) {
        this.itemStack = itemStack;
        this.lore = lore;
    }
    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }
    public void setLore(List<String> lore){
        this.lore = lore;
    }
    public ItemStack getItemStack(Player player) {
        ItemStack clonedItem = itemStack.clone();
        ItemMeta meta = clonedItem.getItemMeta();
        if(meta != null) {
            if(lore != null){
                List<String> parsedLore = lore.stream()
                        .map(line -> PlaceholderAPI.setPlaceholders(player, line))
                        .map(RGBUtils::toChatColorString)
                        .collect(Collectors.toList());
                meta.setLore(parsedLore);
            }
            if(displayName != null){
                meta.setDisplayName(RGBUtils.toChatColorString(PlaceholderAPI.setPlaceholders(player, displayName)));
            }
            clonedItem.setItemMeta(meta);
        }
        return clonedItem;
    }
}