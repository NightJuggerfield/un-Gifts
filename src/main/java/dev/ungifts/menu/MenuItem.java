package dev.ungifts.menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.function.Consumer;
public class MenuItem {
    private final int slot;
    private final MenuItemData menuItemData;
    private final Consumer<Player> action;
    public MenuItem(int slot, MenuItemData menuItemData, Consumer<Player> action) {
        this.slot = slot;
        this.menuItemData = menuItemData;
        this.action = action;
    }
    public MenuItem(int slot, ItemStack itemStack, Consumer<Player> action) {
        this.slot = slot;
        this.menuItemData = new MenuItemData(itemStack, null);
        this.action = action;
    }
    public MenuItem(int slot, ItemStack itemStack, List<String> lore, Consumer<Player> action) {
        this.slot = slot;
        this.menuItemData = new MenuItemData(itemStack, lore);
        this.action = action;
    }
    public int getSlot() {
        return slot;
    }
    public MenuItemData getMenuItemData() {
        return menuItemData;
    }
    public void onClick(Player player) {
        action.accept(player);
    }
    public ItemStack getItemStack(Player player) {
        return menuItemData.getItemStack(player);
    }
    public void setDisplayName(String name){
        menuItemData.setDisplayName(name);
    }
    public void setLore(List<String> lore){
        menuItemData.setLore(lore);
    }
}