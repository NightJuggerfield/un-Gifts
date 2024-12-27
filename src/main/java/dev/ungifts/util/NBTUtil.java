package dev.ungifts.util;
import org.bukkit.inventory.ItemStack;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import dev.ungifts.Main;
public class NBTUtil {
    private static final Logger LOGGER = Main.getPlugin(Main.class).getLogger();
    private static final String NBT_TAG_COMPOUND = "net.minecraft.nbt.NBTTagCompound";
    private static final String MOJANGSON_PARSER = "net.minecraft.nbt.MojangsonParser";
    private static final String NMS_ITEM_STACK = "net.minecraft.world.item.ItemStack";
    private static final String CRAFT_ITEM_STACK = "org.bukkit.craftbukkit.%s.inventory.CraftItemStack";
    private static Method parseMethod;
    private static Method setTagMethod;
    private static Method asNMSCopyMethod;
    private static Method asBukkitCopyMethod;
    private static boolean isInitialized = false;
    static {
        initialize();
    }
    private static synchronized void initialize() {
        if (isInitialized) {
            return;
        }
        try {
            String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> nbtTagCompoundClass = Class.forName(NBT_TAG_COMPOUND);
            Class<?> mojangsonParserClass = Class.forName(MOJANGSON_PARSER);
            parseMethod = mojangsonParserClass.getMethod("parse", String.class);
            Class<?> craftItemStackClass = Class.forName(String.format(CRAFT_ITEM_STACK, version));
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClass = Class.forName(NMS_ITEM_STACK);
            setTagMethod = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
            asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            isInitialized = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Не удалось инициализировать NBTUtil для текущей версии сервера! NBT функционал будет отключен.", e);
            isInitialized = false;
        }
    }
    public static ItemStack applyNBT(ItemStack itemStack, String nbtString) {
        if (!isInitialized) {
            LOGGER.log(Level.WARNING, "NBTUtil не инициализирован. Возвращаю оригинальный ItemStack.");
            return itemStack;
        }
        if (nbtString == null || nbtString.isEmpty()) {
            return itemStack;
        }
        try {
            Object nmsStack = asNMSCopyMethod.invoke(null, itemStack);
            if (nmsStack == null) {
                LOGGER.log(Level.WARNING, "asNMSCopy вернул null");
                return itemStack;
            }
            Object nbt = parseMethod.invoke(null, nbtString);
            if (nbt == null) {
                LOGGER.log(Level.WARNING, "parseMethod вернул null");
                return itemStack;
            }
            setTagMethod.invoke(nmsStack, nbt);
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsStack);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Ошибка при применении NBT-тега: " + nbtString, e);
            return itemStack;
        }
    }
}