package studio.jan1k.boosterrewards.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import studio.jan1k.boosterrewards.BoosterReward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker implements Listener {

    private final BoosterReward plugin;
    private final String modrinthId = "h34wc4II";
    private final String githubRepo = "Jan1k1/BoosterRewards";
    private String latestVersion;
    private boolean updateAvailable = false;

    public UpdateChecker(BoosterReward plugin) {
        this.plugin = plugin;
        checkForUpdates();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                // Try Modrinth First
                String version = getModrinthVersion();
                if (version == null) {
                    // Fallback to GitHub
                    version = getGithubVersion();
                }

                if (version != null) {
                    latestVersion = version;
                    String currentVersion = plugin.getDescription().getVersion();
                    // Simple version comparison (removes 'v' prefix if present)
                    String cleanLatest = latestVersion.replace("v", "");
                    String cleanCurrent = currentVersion.replace("v", "");

                    if (!cleanLatest.equalsIgnoreCase(cleanCurrent)) {
                        updateAvailable = true;
                        Logs.warn("A new version of BoosterRewards is available: " + latestVersion);
                        Logs.warn("Download it at: https://modrinth.com/plugin/boosterrewards");
                    }
                }
            } catch (Exception e) {
                Logs.error("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getModrinthVersion() {
        try {
            URL url = new URL("https://api.modrinth.com/v2/project/" + modrinthId + "/version");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON manually to avoid Gson/Jackson complexity if possible,
                // but we have Gson shaded in plugin. Assume simple search for "version_number"
                String json = response.toString();
                // Extremely basic parsing to get the first version_number
                // ("version_number":"x.x.x")
                int index = json.indexOf("\"version_number\":\"");
                if (index != -1) {
                    int start = index + 18;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Modrinth failed, return null to try GitHub
            return null;
        }
        return null;
    }

    private String getGithubVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();
                int index = json.indexOf("\"tag_name\":\"");
                if (index != -1) {
                    int start = index + 12;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable)
            return;
        if (event.getPlayer().hasPermission("boosterrewards.admin")) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "--------------------------------------------------");
            event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "BoosterRewards " + ChatColor.GRAY + "» "
                    + ChatColor.GREEN + "A new update is available!");
            event.getPlayer().sendMessage(
                    ChatColor.GRAY + "Current version: " + ChatColor.RED + plugin.getDescription().getVersion());
            event.getPlayer().sendMessage(ChatColor.GRAY + "New version: " + ChatColor.GREEN + latestVersion);
            event.getPlayer().sendMessage(
                    ChatColor.GRAY + "Download: " + ChatColor.AQUA + "https://modrinth.com/plugin/boosterrewards");
            event.getPlayer().sendMessage(ChatColor.GRAY + "--------------------------------------------------");
        }
    }
}
