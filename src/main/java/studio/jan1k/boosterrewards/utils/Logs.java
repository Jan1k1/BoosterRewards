package studio.jan1k.boosterrewards.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logs {

    private static final String PREFIX = "&d&lBoosterRewards &8» &f";

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + message));
    }

    public static void success(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&a" + message));
    }

    public static void error(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&c" + message));
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&e" + message));
    }

    public static void debug(String message) {
        if (Bukkit.getPluginManager().getPlugin("BoosterRewards").getConfig().getBoolean("debug", false)) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&b[DEBUG] " + message));
        }
    }

    public static void raw(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void bannerAccent(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
