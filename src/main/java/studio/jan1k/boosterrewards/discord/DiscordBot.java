package studio.jan1k.boosterrewards.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.modals.Modal;
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
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::startBot);
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
                            .addOption(OptionType.STRING, "code",
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
        Guild guild = member.getGuild();
        boolean isNowBoosting = member.getTimeBoosted() != null;

        // Calculate boost count: JDA Member doesn't have getBoostCount(),
        // but Guild.getBoosters() contains a Member for each active boost.
        int boostCount = 0;
        for (Member booster : guild.getBoosters()) {
            if (booster.getId().equals(discordId)) {
                boostCount++;
            }
        }

        UUID uuid = plugin.getDatabaseManager().getUuid(discordId);
        if (uuid == null)
            return;

        if (isNowBoosting) {
            Logs.info(
                    "User " + member.getEffectiveName() + " started boosting in Discord! (Boosts: " + boostCount + ")");
            plugin.getDatabaseManager().saveBooster(discordId, uuid, member.getEffectiveName(),
                    System.currentTimeMillis(), boostCount);
            plugin.getRewardManager().giveReward(uuid, "booster");

            // If they have 2+ boosts, also give tier 2
            if (boostCount >= 2 && plugin.getConfig().getBoolean("rewards.booster_2.enabled", false)) {
                plugin.getRewardManager().giveReward(uuid, "booster_2");
            }

            announceBoost(member, uuid);
        } else {
            Logs.info("User " + member.getEffectiveName() + " stopped boosting in Discord.");
            plugin.getDatabaseManager().setBoosterInactive(discordId);
            plugin.getRewardManager().revokeReward(uuid, "booster");
            plugin.getRewardManager().revokeReward(uuid, "booster_2");
        }
    }

    private void announceBoost(Member member, UUID uuid) {
        // Boost announcements are handled by RewardManager/Commands now if enabled,
        // or disabled as per user request to remove specific channels.
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("link_account"))
            return;

        String discordId = event.getUser().getId();
        String username = event.getUser().getName();

        if (plugin.getDatabaseManager().getUuid(discordId) != null) {
            event.reply("❌ You are already linked to a Minecraft account!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String mode = plugin.getConfig().getString("linking.mode", "DISCORD_TO_MINECRAFT");

        if (mode.equalsIgnoreCase("DISCORD_TO_MINECRAFT")) {
            String code = linkManager.generateCode(discordId, username);
            event.reply("**Your Link Code:** `" + code + "`\n\n" +
                    "Join the Minecraft server and type:\n" +
                    "`/link " + code + "`")
                    .setEphemeral(true)
                    .queue();
            Logs.info("[Link] Button: Generated code " + code + " for " + username);
        } else {
            // MINECRAFT_TO_DISCORD: Open modal for code entry using Label container (JDA
            // 6.3.0 logic)
            TextInput codeInput = TextInput.create("link_code", TextInputStyle.SHORT)
                    .setPlaceholder("Enter the code from /link in-game")
                    .setMinLength(4)
                    .setMaxLength(8)
                    .setRequired(true)
                    .build();

            // In JDA 6.3.0, TextInput is wrapped in Label and added directly to Modal
            Label labeledInput = Label.of("link_code_label", "Minecraft Link Code", codeInput);

            Modal modal = Modal.create("link_modal", "Link Your Minecraft Account")
                    .addComponents(labeledInput)
                    .build();

            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("link_modal"))
            return;

        String code = event.getValue("link_code").getAsString().toUpperCase();
        String discordId = event.getUser().getId();

        if (plugin.getDatabaseManager().getUuid(discordId) != null) {
            event.reply("❌ You are already linked to a Minecraft account!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String info = linkManager.getInfo(code);
        if (info == null) {
            event.reply("Invalid or expired code. Please generate a new one in-game.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (info.startsWith("MINECRAFT:")) {
            String[] parts = info.split(":");
            String uuidStr = parts[1];
            String mcName = parts[2];
            UUID uuid = UUID.fromString(uuidStr);

            plugin.getDatabaseManager().saveUser(uuid, discordId);
            plugin.cachePlayer(uuid, discordId);
            linkManager.invalidateCode(code);

            event.reply("Successfully linked to Minecraft player **" + mcName + "**!")
                    .setEphemeral(true)
                    .queue();

            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "✓ Your Discord account has been linked!");
                // Trigger immediate boost check
                handleBoostUpdate(event.getGuild().getMemberById(discordId));
            }
        } else {
            event.reply("Invalid code type. Please use the code generated in Minecraft.")
                    .setEphemeral(true)
                    .queue();
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
            code = event.getOption("code").getAsString().toUpperCase();
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
                event.reply("Invalid or expired code. Please generate a new one in Minecraft.")
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

            event.reply("Successfully linked to Minecraft player **" + mcName + "**!")
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
            event.reply("You are not linked to any Minecraft account.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        plugin.getDatabaseManager().removeUser(uuid);
        plugin.removeCachedPlayer(uuid);

        event.reply("Successfully unlinked your account.")
                .setEphemeral(true)
                .queue();
    }

    private void handleStatus(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        UUID uuid = plugin.getDatabaseManager().getUuid(discordId);

        if (uuid == null) {
            event.reply("Your account is not linked. Use `/link` to connect.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        guild.retrieveMemberById(discordId).queue(member -> {
            boolean isBoosting = member.getTimeBoosted() != null;
            String statusValue = isBoosting ? "Boosting" : "Not Boosting";
            event.reply("**BoosterRewards Status**\n" +
                    "• Link: Connected\n" +
                    "• Status: " + statusValue)
                    .setEphemeral(true)
                    .queue();
        }, throwable -> {
            event.reply("Could not retrieve your member data.")
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

    public void postLinkPanel(String channelId, org.bukkit.entity.Player sender) {
        if (jda == null)
            return;

        net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            Logs.error("Could not find channel with ID: " + channelId);
            if (sender != null) {
                sender.sendMessage(org.bukkit.ChatColor.RED + "Could not find Discord channel with that ID!");
            }
            return;
        }

        net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder();
        embed.setTitle("Link Your Minecraft Account");
        embed.setDescription("Click the button below to link your Minecraft account and receive rewards for boosting!");
        embed.setColor(java.awt.Color.decode("#EB459E"));
        embed.setFooter("BoosterRewards", null);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(net.dv8tion.jda.api.components.actionrow.ActionRow.of(
                        Button.primary("link_account", "Link Account")))
                .queue(
                        success -> {
                            Logs.info("Link panel posted to channel " + channel.getName());
                            if (sender != null) {
                                sender.sendMessage(org.bukkit.ChatColor.GREEN + "Successfully posted link panel to #"
                                        + channel.getName() + "!");
                            }
                        },
                        error -> {
                            Logs.error("Failed to post link panel: " + error.getMessage());
                            if (sender != null) {
                                sender.sendMessage(
                                        org.bukkit.ChatColor.RED + "Failed to post link panel: " + error.getMessage());
                            }
                        });
    }
}
