package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.SchedulerUtils;

public class ClaimCommand implements CommandExecutor {

    private final BoosterReward plugin;

    public ClaimCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId());
        if (discordId == null) {
            player.sendMessage(ChatColor.RED + "❌ You must link your account before you can claim rewards!");
            player.sendMessage(ChatColor.GRAY + "Use /link to connect your Discord account.");
            return true;
        }

        if (!plugin.getDatabaseManager().isBoosting(player.getUniqueId())) {
            syncAndOpenGUI(player, discordId);
            return true;
        }

        new studio.jan1k.boosterrewards.gui.ClaimSelectorGUI(plugin, player);

        return true;
    }

    private void syncAndOpenGUI(Player player, String discordId) {
        if (plugin.getDiscordBot() == null || plugin.getDiscordBot().getJDA() == null) {
            player.sendMessage(ChatColor.RED + "❌ Could not verify your boost status. Discord bot is not connected.");
            return;
        }
        String guildId = plugin.getConfigManager().getDiscordGuildId();
        net.dv8tion.jda.api.entities.Guild guild = plugin.getDiscordBot().getJDA().getGuildById(guildId);

        if (guild == null) {
            player.sendMessage(ChatColor.RED + "❌ Could not verify your boost status. Please try again later.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Verifying boost status...");

        guild.retrieveMemberById(discordId).queue(member -> {
            boolean isBoosting = member.getTimeBoosted() != null;
            int boostCount = 0;
            if (isBoosting) {
                for (net.dv8tion.jda.api.entities.Member booster : guild.getBoosters()) {
                    if (booster.getId().equals(discordId)) {
                        boostCount++;
                    }
                }
            }

            if (isBoosting && boostCount == 0)
                boostCount = 1;

            final int finalBoostCount = boostCount;
            plugin.getDatabaseManager().setBoosterStatus(player.getUniqueId(), isBoosting, finalBoostCount);

            SchedulerUtils.runSync(plugin, () -> {
                if (!player.isOnline())
                    return;

                if (isBoosting) {
                    plugin.getRewardManager().giveReward(player.getUniqueId(), "booster");
                    if (finalBoostCount >= 2 && plugin.getConfig().getBoolean("rewards.booster_2.enabled", false)) {
                        plugin.getRewardManager().giveReward(player.getUniqueId(), "booster_2");
                    }
                    new studio.jan1k.boosterrewards.gui.ClaimSelectorGUI(plugin, player, finalBoostCount);
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You are not currently boosting the server!");
                    player.sendMessage(ChatColor.GRAY + "Boost our Discord server to unlock rewards!");
                }
            });
        }, error -> {
            SchedulerUtils.runSync(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.RED + "❌ Failed to retrieve your Discord data.");
                }
            });
        });
    }
}
