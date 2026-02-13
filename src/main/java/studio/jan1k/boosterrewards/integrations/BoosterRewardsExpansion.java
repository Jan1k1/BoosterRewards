package studio.jan1k.boosterrewards.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
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
        return "Jan1k";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("is_boosting")) {
            return String.valueOf(plugin.getDatabaseManager().isBoosting(player.getUniqueId()));
        }

        if (identifier.equals("boost_count")) {
            return String.valueOf(plugin.getDatabaseManager().getBoostCount(player.getUniqueId()));
        }

        if (identifier.equals("discord_id")) {
            String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId());
            return discordId != null ? discordId : "Not Linked";
        }

        return null;
    }
}
