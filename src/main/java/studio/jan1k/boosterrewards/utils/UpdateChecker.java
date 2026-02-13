package studio.jan1k.boosterrewards.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import studio.jan1k.boosterrewards.BoosterReward;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker implements Listener {

    private final BoosterReward plugin;
    private final int resourceId;
    private String latestVersion;

    public UpdateChecker(BoosterReward plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        SchedulerUtils.runAsync(plugin, () -> {
            try (InputStream inputStream = new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Logs.error("Unable to check for updates: " + exception.getMessage());
            }
        });
    }

    public void check() {
        if (!plugin.getConfig().getBoolean("features.update-checker", true))
            return;

        getVersion(version -> {
            this.latestVersion = version;
            String currentVersion = plugin.getDescription().getVersion();

            if (currentVersion.equalsIgnoreCase(version)) {
                Logs.success("You are running the latest version!");
            } else {
                Logs.warn("A new update is available: v" + version);
                Logs.warn("Download it here: https://www.spigotmc.org/resources/" + resourceId);
            }
        });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (latestVersion == null || !player.hasPermission("boosterrewards.admin"))
            return;

        String currentVersion = plugin.getDescription().getVersion();
        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&d&lBoosterRewards &8» &7A new update is available! &f(v" + latestVersion + ")"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&d&lBoosterRewards &8» &7Download: &f&nhttps://www.spigotmc.org/resources/" + resourceId));
        }
    }
}
