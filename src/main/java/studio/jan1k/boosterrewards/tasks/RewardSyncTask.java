package studio.jan1k.boosterrewards.tasks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.PlayerData;

public class RewardSyncTask extends BukkitRunnable {

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

            String discordId = data.getDiscordId();

            // Try to get from JDA cache first (very fast)
            Member member = guild.getMemberById(discordId);
            if (member != null) {
                checkRewards(player, member);
            } else {
                // Not in cache, fetch it (async)
                guild.retrieveMemberById(discordId).queue(m -> {
                    checkRewards(player, m);
                }, error -> {
                });
            }
        }
    }

    private void checkRewards(Player player, Member member) {
        boolean isBooster = member.isBoosting();
        int boostCount = 0;
        if (isBooster) {
            for (Member booster : member.getGuild().getBoosters()) {
                if (booster.getId().equals(member.getId())) {
                    boostCount++;
                }
            }
        }
        plugin.getDatabaseManager().setBoosterStatus(player.getUniqueId(), isBooster, boostCount);
    }
}
