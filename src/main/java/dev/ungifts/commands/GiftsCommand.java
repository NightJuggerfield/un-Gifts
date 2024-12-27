package dev.ungifts.commands;
import dev.ungifts.Main;
import dev.ungifts.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
public class GiftsCommand implements CommandExecutor {
    private final Main plugin;
    public GiftsCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("gifts").setExecutor(this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        Player player = (Player) sender;
        Menu menu = plugin.getGiftManager().getMenu();
        if(menu != null) {
            menu.open(player);
        } else {
            player.sendMessage("Ошибка! Конфигурация меню неправильная.");
        }
        return true;
    }
}