package studio.jan1k.boosterrewards.utils;

import org.bukkit.Bukkit;

public class Logs {

    private static final String RESET = "\u001B[0m";
    private static final String PINK = "\u001B[38;2;227;84;255m";
    private static final String BLUE = "\u001B[38;2;84;165;255m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GRAY = "\u001B[90m";

    public static void info(String message) {
        Bukkit.getLogger().info("▸ " + message);
    }

    public static void success(String message) {
        Bukkit.getLogger().info("✓ " + message);
    }

    public static void warn(String message) {
        Bukkit.getLogger().warning("⚠ " + message);
    }

    public static void error(String message) {
        Bukkit.getLogger().severe("✖ " + message);
    }

    public static void database(String message) {
        Bukkit.getLogger().info("[DATABASE] " + message);
    }

    public static void discord(String message) {
        Bukkit.getLogger().info("[DISCORD] " + message);
    }

    public static void sync(String message) {
        Bukkit.getLogger().info("[SYNC] " + message);
    }

    public static void license(String message) {
        Bukkit.getLogger().info("[LICENSE] " + message);
    }

    public static void raw(String message) {
        bannerAccent(translate(message));
    }

    private static String translate(String message) {
        return message
                .replace("§0", "\u001B[30m")
                .replace("§1", "\u001B[34m")
                .replace("§2", "\u001B[32m")
                .replace("§3", "\u001B[36m")
                .replace("§4", "\u001B[31m")
                .replace("§5", "\u001B[35m")
                .replace("§6", "\u001B[33m")
                .replace("§7", "\u001B[37m")
                .replace("§8", "\u001B[90m")
                .replace("§9", "\u001B[94m")
                .replace("§a", "\u001B[92m")
                .replace("§b", "\u001B[96m")
                .replace("§c", "\u001B[91m")
                .replace("§d", "\u001B[95m")
                .replace("§e", "\u001B[93m")
                .replace("§f", "\u001B[97m")
                .replace("§r", RESET)
                .replace("§l", "\u001B[1m")
                .replace("§o", "\u001B[3m")
                .replace("§n", "\u001B[4m")
                .replace("§m", "\u001B[9m")
                .replace("§k", "\u001B[5m");
    }

    public static void bannerAccent(String message) {
        Bukkit.getConsoleSender().sendMessage(message + RESET);
    }
}
