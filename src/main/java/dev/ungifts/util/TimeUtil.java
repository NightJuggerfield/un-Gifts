package dev.ungifts.util;

import org.bukkit.configuration.ConfigurationSection;

public class TimeUtil {
    private static final String TIME_FORMAT_PATH = "gifts.time-format";

    public static String formatTime(long milliseconds, ConfigurationSection config) {
        ConfigurationSection timeFormatConfig = config.getConfigurationSection(TIME_FORMAT_PATH);
        if (timeFormatConfig == null) {
            throw new IllegalArgumentException("Time format configuration section is missing");
        }

        if (milliseconds <= 0) {
            return "0" + timeFormatConfig.getString("sec", "%s сек.");
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        StringBuilder sb = new StringBuilder();

        if (months > 0) {
            sb.append(timeFormatConfig.getString("mon", "%mon мес. ").replace("%mon", String.valueOf(months)));
            days = days % 30;
        }
        if (days > 0) {
            sb.append(timeFormatConfig.getString("day", "%day дн. ").replace("%day", String.valueOf(days)));
            hours = hours % 24;
        }
        if (hours > 0) {
            sb.append(timeFormatConfig.getString("hou", "%hou ч. ").replace("%hou", String.valueOf(hours)));
            minutes = minutes % 60;
        }
        if (minutes > 0) {
            sb.append(timeFormatConfig.getString("min", "%m м. ").replace("%m", String.valueOf(minutes)));
            seconds = seconds % 60;
        }
        if (seconds > 0) {
            sb.append(timeFormatConfig.getString("sec", "%s сек.").replace("%s", String.valueOf(seconds)));
        }

        return sb.toString().trim();
    }
}
