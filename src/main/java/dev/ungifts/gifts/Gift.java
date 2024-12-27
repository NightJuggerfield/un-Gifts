package dev.ungifts.gifts;
import dev.ungifts.Main;
import dev.ungifts.util.NBTUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.UUID;
public class Gift {
    private final GiftType type;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final List<String> commands;
    private final List<String> delayMessage;
    private final String nbtString;
    public Gift(GiftType type, Material material, String name, List<String> lore, List<String> commands, List<String> delayMessage, String nbtString) {
        this.type = type;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.commands = commands;
        this.delayMessage = delayMessage;
        this.nbtString = nbtString;
    }
    public GiftType getType() {
        return type;
    }
    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (name != null) {
                itemMeta.setDisplayName(name);
            }
            if (lore != null && !lore.isEmpty()) {
                itemMeta.setLore(lore);
            }
            if(nbtString != null && !nbtString.isEmpty()) {
                itemStack = NBTUtil.applyNBT(itemStack, nbtString);
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
    public long getDelay(UUID uuid, Main plugin) {
        long lastClaim = plugin.getDataStorage().getLastClaimTime(uuid, type.toString());
        switch (type) {
            case DAILY:
                return lastClaim + 24 * 60 * 60 * 1000 - System.currentTimeMillis();
            case WEEKLY:
                return lastClaim + 7 * 24 * 60 * 60 * 1000 - System.currentTimeMillis();
            case MONTHLY:
                return lastClaim + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis();
            default:
                return 0;
        }
    }
    public List<String> getDelayMessage() {
        return delayMessage;
    }
    public List<String> getCommands() {
        return commands;
    }
}