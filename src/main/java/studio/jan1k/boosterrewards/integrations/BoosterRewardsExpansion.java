package studio.jan1k.boosterrewards.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import studio.jan1k.boosterrewards.BoosterReward;

public class BoosterRewardsExpansion extends PlaceholderExpansion {

    private final BoosterReward plugin;

    public BoosterRewardsExpansion(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "boosterrewards";
    }

    @Override
    public @NotNull String getAuthor() {
        return "jan1k";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null)
            return "";

        if (params.equalsIgnoreCase("linked")) {
            return plugin.getDatabaseManager().getDiscordId(player.getUniqueId()) != null ? "yes" : "no";
        }

        if (params.equalsIgnoreCase("discord_id")) {
            String id = plugin.getDatabaseManager().getDiscordId(player.getUniqueId());
            return id != null ? id : "None";
        }

        if (params.equalsIgnoreCase("is_booster")) {
            // This is a simplified check. We'd ideally check if active in DB.
            // For now, let's check if they have a non-zero boost start in boosters table
            // or use the count logic.
            return plugin.getDatabaseManager()
                    .getUuid(plugin.getDatabaseManager().getDiscordId(player.getUniqueId())) != null ? "yes" : "no";
        }

        return null;
    }
}
