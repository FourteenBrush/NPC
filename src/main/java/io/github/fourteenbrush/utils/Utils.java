package io.github.fourteenbrush.utils;

import io.github.fourteenbrush.NPCPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private Utils() {}

    public static String colorize(String args) {
        return ChatColor.translateAlternateColorCodes('&', args);
    }

    public static String[] colorize(String... args) {
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = colorize(args[i]);
        }
        return result;
    }

    public static void logInfo(String... messages) {
        for (String message : messages) {
            log(LogLevel.INFO, message);
        }
    }

    public static void logWarning(String... messages) {
        for (String message : messages) {
            log(LogLevel.WARNING, message);
        }
    }

    public static void logDebug(String... messages) {
        for (String message : messages) {
            log(LogLevel.DEBUG, message);
        }
    }

    public static void logError(String... messages) {
        for (String message: messages) {
            log(LogLevel.ERROR, message);
        }
    }

    public static void logFatal(Throwable th ,String... messages) {
        for (String message : messages) {
            log(LogLevel.ERROR, message);
        }
        th.printStackTrace();
        log(LogLevel.WARNING, "&cDisabling myself...");
        Bukkit.getPluginManager().disablePlugin(NPCPlugin.getInstance());
    }

    private static void log(LogLevel level, String message) {
        Bukkit.getConsoleSender().sendMessage(colorize("&7[&cMagmaBuildNetwork&7] " + "[" + level.name() + "] " + level.getColor() + message));
    }

    public static String getFinalArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (; start < args.length; start++) {
            builder.append(args[start]).append(" ");
        }
        return builder.toString().trim();
    }

    public static String randomCharacters(int length) {
        Validate.isTrue(length > 0, "Invalid length. Length must be at least 1 characters");
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = ThreadLocalRandom.current();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    private enum LogLevel {
        INFO, WARNING, ERROR, DEBUG;

        private ChatColor getColor() {
            switch (this) {
                case WARNING:
                case ERROR:
                    return ChatColor.RED;
                case DEBUG:
                    return ChatColor.BLUE;
                default:
                    return ChatColor.GRAY;
            }
        }
    }
}
