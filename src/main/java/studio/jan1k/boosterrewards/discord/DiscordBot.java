package studio.jan1k.boosterrewards.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.LinkManager;
import studio.jan1k.boosterrewards.utils.Logs;

import java.util.UUID;

public class DiscordBot extends ListenerAdapter {

    private final BoosterReward plugin;
    private final LinkManager linkManager;
    private JDA jda;

    public DiscordBot(BoosterReward plugin, LinkManager linkManager) {
        this.plugin = plugin;
        this.linkManager = linkManager;
        startBot();
    }

    private void startBot() {
        try {
            String token = plugin.getConfigManager().getDiscordToken();
            if (token == null || token.equals("YOUR_BOT_TOKEN_HERE") || token.trim().isEmpty()) {
                Logs.error("Discord Bot Token is empty or invalid! Please set it in discord.yml");
                return;
            }

            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(this)
                    .build();

            jda.awaitReady();
            registerCommands();
        } catch (Exception e) {
            Logs.error("Failed to start Discord Bot!");
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.equals("000000000000000000") || guildId.isEmpty()) {
            return;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.updateCommands().addCommands(
                    Commands.slash("link", "Link your Minecraft account")
                            .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "code",
                                    "The link code from Minecraft", false),
                    Commands.slash("unlink", "Unlink your Minecraft account"),
                    Commands.slash("status", "Check your boost status")).queue();
        }
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        handleBoostUpdate(event.getMember());
    }

    private void handleBoostUpdate(Member member) {
        if (member == null)
            return;

        String discordId = member.getId();
        boolean isNowBoosting = member.getTimeBoosted() != null;

        UUID uuid = plugin.getDatabaseManager().getUuid(discordId);
        if (uuid == null)
            return;

        if (isNowBoosting) {
            Logs.info("User " + member.getEffectiveName() + " started boosting in Discord!");
            plugin.getDatabaseManager().saveBooster(discordId, uuid, member.getEffectiveName(),
                    System.currentTimeMillis());
            plugin.getRewardManager().giveReward(uuid, "booster");
            announceBoost(member, uuid);
        } else {
            Logs.info("User " + member.getEffectiveName() + " stopped boosting in Discord.");
            plugin.getDatabaseManager().setBoosterInactive(discordId);
            plugin.getRewardManager().revokeReward(uuid, "booster");
        }
    }

    private void announceBoost(Member member, UUID uuid) {
        String mcName = org.bukkit.Bukkit.getOfflinePlayer(uuid).getName();
        String discordName = member.getEffectiveName();

        // 1. Announce to Discord
        String channelId = plugin.getConfig().getString("announcements.discord.channel-id");
        if (channelId != null && !channelId.isEmpty() && !channelId.equals("000000000000000000")) {
            net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                String msg = plugin.getConfig().getString("announcements.discord.message",
                        "🌟 **%player%** just boosted the server!")
                        .replace("%player%", discordName)
                        .replace("%mc_name%", mcName != null ? mcName : "Unknown");
                channel.sendMessage(msg).queue();
            }
        }

        // 2. Announce to Minecraft
        String mcMsg = plugin.getConfig().getString("announcements.minecraft.message",
                "&d&lBooster &8» &f%player% &7just boosted the server! &d❤")
                .replace("%player%", mcName != null ? mcName : discordName)
                .replace("%discord_name%", discordName);

        org.bukkit.Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', mcMsg));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        switch (cmd) {
            case "link":
                handleLink(event);
                break;
            case "unlink":
                handleUnlink(event);
                break;
            case "status":
                handleStatus(event);
                break;
        }
    }

    private void handleLink(SlashCommandInteractionEvent event) {
        String mode = plugin.getConfig().getString("linking.mode", "MINECRAFT_TO_DISCORD");
        String discordId = event.getUser().getId();
        String username = event.getUser().getName();

        if (plugin.getDatabaseManager().getUuid(discordId) != null) {
            event.reply("❌ You are already linked to a Minecraft account!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String code = null;
        if (event.getOption("code") != null) {
            code = event.getOption("code").getAsString();
        }

        if (mode.equalsIgnoreCase("MINECRAFT_TO_DISCORD")) {
            if (code == null) {
                event.reply("Please provide the code generated in Minecraft! Run `/link` in-game first.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String info = linkManager.getInfo(code);
            if (info == null || !info.startsWith("MINECRAFT:")) {
                event.reply("❌ Invalid or expired code. Please generate a new one in Minecraft.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String[] parts = info.split(":");
            String uuidStr = parts[1];
            String mcName = parts[2];
            UUID uuid = UUID.fromString(uuidStr);

            plugin.getDatabaseManager().saveUser(uuid, discordId);
            plugin.cachePlayer(uuid, discordId);
            linkManager.invalidateCode(code);

            event.reply("✅ Successfully linked to Minecraft player **" + mcName + "**!")
                    .setEphemeral(true)
                    .queue();

            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "Linked to Discord user: " + username);
            }
        } else {
            String generatedCode = linkManager.generateCode(discordId, username);
            event.reply("Your Link Code: `" + generatedCode + "`\nRun `/link " + generatedCode + "` in Minecraft.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleUnlink(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        UUID uuid = plugin.getDatabaseManager().getUuid(discordId);

        if (uuid == null) {
            event.reply("❌ You are not linked to any Minecraft account.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        plugin.getDatabaseManager().removeUser(uuid);
        plugin.removeCachedPlayer(uuid);

        event.reply("✅ Successfully unlinked your account.")
                .setEphemeral(true)
                .queue();
    }

    private void handleStatus(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        UUID uuid = plugin.getDatabaseManager().getUuid(discordId);

        if (uuid == null) {
            event.reply("❌ Your account is not linked. Use `/link` to connect.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        guild.retrieveMemberById(discordId).queue(member -> {
            boolean isBoosting = member.getTimeBoosted() != null;
            String statusValue = isBoosting ? "💎 Boosting" : "❌ Not Boosting";
            event.reply("🔍 **BoosterRewards Status**\n" +
                    "• Link: ✅ Connected\n" +
                    "• Status: " + statusValue)
                    .setEphemeral(true)
                    .queue();
        }, throwable -> {
            event.reply("❌ Could not retrieve your member data.")
                    .setEphemeral(true)
                    .queue();
        });
    }

    public void stop() {
        if (jda != null)
            jda.shutdown();
    }

    public JDA getJDA() {
        return jda;
    }
}
