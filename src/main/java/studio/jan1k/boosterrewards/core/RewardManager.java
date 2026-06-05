package studio.jan1k.boosterrewards.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import studio.jan1k.boosterrewards.BoosterReward;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import studio.jan1k.boosterrewards.utils.SchedulerUtils;

public class RewardManager {

    private final BoosterReward plugin;
    private final Set<String> deliveriesInProgress = ConcurrentHashMap.newKeySet();

    public RewardManager(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void giveReward(UUID uuid, String tier) {
        String deliveryKey = uuid + ":" + tier;
        if (!deliveriesInProgress.add(deliveryKey))
            return;

        SchedulerUtils.runAsync(plugin, () -> {
            boolean handedOffToSync = false;
            try {
                if (plugin.getDatabaseManager().hasAlreadyClaimed(uuid, tier))
                    return;

                List<String> commands = plugin.getConfig().getStringList("rewards." + tier + ".on-boost");
                String discordId = plugin.getDatabaseManager().getDiscordId(uuid);
                plugin.getItemRewardHandler().queueItemRewards(uuid, uuid.toString(), tier);

                handedOffToSync = true;
                SchedulerUtils.runSync(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        deliveriesInProgress.remove(deliveryKey);
                        return;
                    }

                    boolean delivered = true;
                    for (String cmd : commands) {
                        delivered &= executeCommand(player, cmd, discordId);
                    }

                    if (delivered) {
                        SchedulerUtils.runAsync(plugin, () -> {
                            try {
                                plugin.getDatabaseManager().addClaimRecord(uuid, tier);
                            } finally {
                                deliveriesInProgress.remove(deliveryKey);
                            }
                        });
                    } else {
                        deliveriesInProgress.remove(deliveryKey);
                    }
                });
            } finally {
                if (!handedOffToSync) {
                    deliveriesInProgress.remove(deliveryKey);
                }
            }
        });
    }

    public void revokeReward(UUID uuid, String tier) {
        SchedulerUtils.runAsync(plugin, () -> {
            List<String> commands = plugin.getConfig().getStringList("rewards." + tier + ".on-stop");
            String discordId = plugin.getDatabaseManager().getDiscordId(uuid);

            SchedulerUtils.runSync(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    return;

                for (String cmd : commands) {
                    executeCommand(player, cmd, discordId);
                }
            });
        });
    }

    private boolean executeCommand(Player player, String command, String discordId) {
        String processed = replacePlaceholders(command, player, discordId);

        if (processed.startsWith("player: ")) {
            return player.performCommand(processed.substring(8));
        } else if (processed.startsWith("console: ")) {
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed.substring(9));
        } else {
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
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
