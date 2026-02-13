package studio.jan1k.boosterrewards.tasks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.PlayerData;

import java.util.Objects;

public class RewardSyncTask implements Runnable {

    private final BoosterReward plugin;

    public RewardSyncTask(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getDiscordBot() == null || plugin.getDiscordBot().getJDA() == null)
            return;

        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.trim().isEmpty())
            return;

        Guild guild = plugin.getDiscordBot().getJDA().getGuildById(guildId);
        if (guild == null)
            return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerData(player.getUniqueId());
            if (data == null || data.getDiscordId() == null)
                continue;

            String discordId = Objects.requireNonNull(data.getDiscordId());

            Member member = guild.getMemberById(discordId);
            if (member != null) {
                syncSingle(player, member, guild);
            } else {
                guild.retrieveMemberById(Objects.requireNonNull(discordId)).queue(m -> syncSingle(player, m, guild),
                        error -> {
                        });
            }
        }
    }

    private void syncSingle(Player player, Member member, Guild guild) {
        boolean isBoosting = member.getTimeBoosted() != null;
        int boostCount = (int) guild.getBoosters().stream().filter(m -> m.getId().equals(member.getId())).count();
        if (isBoosting && boostCount == 0)
            boostCount = 1;

        checkRewards(player, isBoosting, boostCount);
    }

    private void checkRewards(Player player, boolean isBooster, int boostCount) {
        plugin.getDatabaseManager().setBoosterStatus(player.getUniqueId(), isBooster, boostCount);

        if (isBooster) {
            org.bukkit.configuration.ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("rewards");
            if (tiers != null) {
                for (String tier : tiers.getKeys(false)) {
                    if (plugin.getConfig().getBoolean("rewards." + tier + ".enabled", false)) {
                        if (tier.equals("booster") || (tier.equals("booster_2") && boostCount >= 2)) {
                            plugin.getRewardManager().giveReward(player.getUniqueId(), tier);
                        }
                    }
                }
            }
        }
    }
}
