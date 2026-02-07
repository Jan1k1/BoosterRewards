package studio.jan1k.boosterrewards.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.LinkManager;

import java.util.UUID;

public class LinkCommand implements CommandExecutor {

    private final BoosterReward plugin;

    public LinkCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String mode = plugin.getConfig().getString("linking.mode", "MINECRAFT_TO_DISCORD");

        if (plugin.getDatabaseManager().getDiscordId(player.getUniqueId()) != null) {
            String msg = plugin.getConfig().getString("messages.in-game.link.already-linked",
                    "&cYou are already linked! Use /logout to unlink.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        if (mode.equalsIgnoreCase("MINECRAFT_TO_DISCORD")) {
            // Mode: MC -> Discord
            // Usage: /link (generates code)

            if (args.length != 0) {
                player.sendMessage(ChatColor.RED + "Usage: /link");
                player.sendMessage(ChatColor.GRAY + "This generates a code to use in Discord.");
                return true;
            }

            String code = plugin.getLinkManager().generateMinecraftCode(player.getUniqueId(), player.getName());

            TextComponent message = new TextComponent("Your link code is: ");
            message.setColor(net.md_5.bungee.api.ChatColor.GREEN);

            TextComponent codeComponent = new TextComponent(code);
            codeComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            codeComponent.setBold(true);
            codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code));
            codeComponent.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy").create()));

            message.addExtra(codeComponent);

            player.spigot().sendMessage(message);
            player.sendMessage(ChatColor.GRAY + "Run " + ChatColor.YELLOW + "/link " + code + ChatColor.GRAY
                    + " in our Discord server.");

        } else {
            // Mode: Discord -> MC
            // Usage: /link <code>

            if (args.length != 1) {
                String msg = plugin.getConfig().getString("messages.in-game.link.usage", "&cUsage: /link <code>");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            String code = args[0];

            // Run everything else ASYNC to prevent main-thread lag
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                LinkManager linkManager = plugin.getLinkManager();
                String discordInfo = linkManager.getDiscordInfo(code);

                if (discordInfo == null) {
                    // String msg = plugin.getConfigManager().getInGameMessage("link.invalid-code");
                    String msg = plugin.getConfig().getString("messages.in-game.link.invalid-code",
                            "&cInvalid or expired link code.");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    return;
                }

                String[] parts = discordInfo.split(":");
                String discordId = parts[0];
                String username = parts[1];

                // DB check for existing discord ID
                if (plugin.getDatabaseManager().getUuid(discordId) != null) {
                    player.sendMessage(ChatColor.RED + "This Discord account is already linked to another player.");
                    return;
                }

                // Save to DB
                // Save Logic needs to ensure UUID is unique too, mostly handled by DB
                // constraints or getDiscordId check above
                plugin.getDatabaseManager().saveUser(player.getUniqueId(), discordId);

                // Update Cache
                plugin.cachePlayer(player.getUniqueId(), discordId);

                linkManager.invalidateCode(code);

                // String msg = plugin.getConfigManager().getInGameMessage("link.success")
                String msg = plugin.getConfig()
                        .getString("messages.in-game.link.success",
                                "&aSuccessfully linked with Discord user &f%discord_user%&a!")
                        .replace("%discord_user%", username);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            });
        }

        return true;
    }
}
