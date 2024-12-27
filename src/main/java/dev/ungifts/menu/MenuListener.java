package dev.ungifts.menu;
import dev.ungifts.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
public class MenuListener implements Listener {
    private final Main plugin;
    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Menu)) return;
        Menu menu = (Menu) holder;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        for (MenuItem item : menu.getItems()) {
            if (item.getSlot() == slot) {
                item.onClick((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
                return;
            }
        }
    }
}