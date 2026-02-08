package studio.jan1k.boosterrewards.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import studio.jan1k.boosterrewards.BoosterReward;

import java.util.List;
import java.util.UUID;

public class RewardManager {

    private final BoosterReward plugin;

    public RewardManager(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void giveReward(UUID uuid, String tier) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Check if player already has rewards of this tier pending
            if (plugin.getDatabaseManager().hasPendingReward(uuid, tier)) {
                return;
            }

            List<String> commands = plugin.getConfig().getStringList("rewards." + tier + ".on-boost");
            String discordId = plugin.getDatabaseManager().getDiscordId(uuid);

            // Execute commands on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    return;

                // 1. Run Commands
                for (String cmd : commands) {
                    executeCommand(player, cmd, discordId);
                }

                // 2. Queue Custom Items as pending rewards
                plugin.getItemRewardHandler().queueItemRewards(player, tier);
            });
        });
    }

    public void revokeReward(UUID uuid, String tier) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> commands = plugin.getConfig().getStringList("rewards." + tier + ".on-stop");
            String discordId = plugin.getDatabaseManager().getDiscordId(uuid);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    return;

                for (String cmd : commands) {
                    executeCommand(player, cmd, discordId);
                }
            });
        });
    }

    private void executeCommand(Player player, String command, String discordId) {
        String processed = replacePlaceholders(command, player, discordId);

        if (processed.startsWith("player: ")) {
            player.performCommand(processed.substring(8));
        } else if (processed.startsWith("console: ")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed.substring(9));
        } else {
            // Default to console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        }
    }

    public String replacePlaceholders(String text, Player player, String discordId) {
        String processed = text
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%discord_id%", discordId != null ? discordId : "N/A");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }
        return processed;
    }
}
