package studio.jan1k.boosterrewards.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.LinkManager;

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

        if (plugin.getPlayerData(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getInGameMessage("link.already-linked")));
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
            player.sendMessage(ChatColor.GREEN + "Your link code is: " + ChatColor.AQUA + ChatColor.BOLD + code);
            player.sendMessage(ChatColor.GRAY + "Run " + ChatColor.YELLOW + "/link " + code + ChatColor.GRAY
                    + " in our Discord server.");

        } else {
            // Mode: Discord -> MC
            // Usage: /link <code>

            if (args.length != 1) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfigManager().getInGameMessage("link.usage")));
                return true;
            }

            String code = args[0];

            // Run everything else ASYNC to prevent main-thread lag
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                LinkManager linkManager = plugin.getLinkManager();
                String discordInfo = linkManager.getDiscordInfo(code);

                if (discordInfo == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfigManager().getInGameMessage("link.invalid-code")));
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
                plugin.getDatabaseManager().saveUser(player.getUniqueId(), discordId);

                // Update Cache
                plugin.cachePlayer(player.getUniqueId(), discordId);

                linkManager.invalidateCode(code);

                String msg = plugin.getConfigManager().getInGameMessage("link.success")
                        .replace("%discord_user%", username);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            });
        }

        return true;
    }
}

