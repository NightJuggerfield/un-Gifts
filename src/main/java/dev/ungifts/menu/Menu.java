package dev.ungifts.menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import java.util.List;
public class Menu implements InventoryHolder {
    private final String name;
    private final List<MenuItem> items;
    private final Inventory inventory;
    public Menu(String name, List<MenuItem> items) {
        this.name = name;
        this.items = items;
        this.inventory = Bukkit.createInventory(this, calculateSize(), name);
        applyItems(null);
    }
    private void applyItems(Player player) {
        for(MenuItem item : items) {
            inventory.setItem(item.getSlot(), item.getItemStack(player));
        }
    }
    public void update(Player player){
        applyItems(player);
    }
    private int calculateSize() {
        int maxSize = 0;
        for(MenuItem item : items) {
            maxSize = Math.max(maxSize, item.getSlot());
        }
        return ((maxSize / 9) + 1) * 9;
    }
    public void open(Player player) {
        applyItems(player);
        player.openInventory(inventory);
    }

    public List<MenuItem> getItems() {
        return items;
    }
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}