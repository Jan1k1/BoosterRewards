package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import studio.jan1k.boosterrewards.BoosterReward;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final BoosterReward plugin;

    public MainCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("boosterrewards.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getConfigManager().loadFullConfigs();
                sender.sendMessage(ChatColor.GREEN + "BoosterRewards configuration reloaded!");
                break;
            case "link":
                // Forward arguments (remove 'link')
                String[] linkArgs = Arrays.copyOfRange(args, 1, args.length);
                new LinkCommand(plugin).onCommand(sender, command, "link", linkArgs);
                break;
            case "unlink":
                if (args.length > 1) {
                    // Admin force unlink: /br unlink <player>
                    if (!sender.hasPermission("boosterrewards.admin")) {
                        sender.sendMessage(ChatColor.RED + "No permission.");
                        return true;
                    }
                    String targetName = args[1];
                    org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                    if (target == null) { // Deprecated but standard for offline players by name
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    java.util.UUID uuid = target.getUniqueId();
                    String discordId = plugin.getDatabaseManager().getDiscordId(uuid);

                    if (discordId == null) {
                        sender.sendMessage(ChatColor.RED + targetName + " is not linked to any Discord account.");
                        return true;
                    }

                    plugin.getDatabaseManager().removeUser(uuid);
                    plugin.removeCachedPlayer(uuid);
                    sender.sendMessage(ChatColor.GREEN + "Successfully unlinked " + targetName + ".");

                    if (target.isOnline()) {
                        ((org.bukkit.entity.Player) target)
                                .sendMessage(ChatColor.RED + "Your account has been unlinked by an administrator.");
                    }
                } else {
                    // Self unlink
                    new UnlinkCommand(plugin).onCommand(sender, command, "unlink", new String[0]);
                }
                break;
            case "claim":
                new ClaimCommand(plugin).onCommand(sender, command, "claim", new String[0]);
                break;
            case "admin":
            case "setboosterreward":
                new SetBoosterRewardCommand(plugin).onCommand(sender, command, "setboosterreward", new String[0]);
                break;
            case "resetclaim":
                if (!sender.hasPermission("boosterrewards.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /br resetclaim <player> [tier]");
                    return true;
                }
                handleResetClaim(sender, args);
                break;
            case "stats":
                if (!sender.hasPermission("boosterrewards.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /br stats <player>");
                    return true;
                }
                handleStats(sender, args);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&d&lBoosterRewards &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatColor.GRAY + "Available commands:");
        sender.sendMessage(ChatColor.WHITE + "/br link " + ChatColor.GRAY + "- Link account");
        sender.sendMessage(ChatColor.WHITE + "/br unlink " + ChatColor.GRAY + "- Unlink account");
        sender.sendMessage(ChatColor.WHITE + "/br claim " + ChatColor.GRAY + "- Claim rewards");
        if (sender.hasPermission("boosterrewards.admin")) {
            sender.sendMessage(ChatColor.WHITE + "/br reload " + ChatColor.GRAY + "- Reload config");
            sender.sendMessage(ChatColor.WHITE + "/br admin " + ChatColor.GRAY + "- Open Admin Panel");
            sender.sendMessage(ChatColor.WHITE + "/br stats <player> " + ChatColor.GRAY + "- View player sync stats");
            sender.sendMessage(
                    ChatColor.WHITE + "/br resetclaim <player> [tier] " + ChatColor.GRAY + "- Reset claim history");
        }
    }

    private void handleResetClaim(CommandSender sender, String[] args) {
        org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        java.util.UUID uuid = target.getUniqueId();
        String tier = args.length > 2 ? args[2] : null;

        if (tier != null) {
            plugin.getDatabaseManager().removeClaimRecord(uuid, tier);
            sender.sendMessage(ChatColor.GREEN + "Reset claim for " + target.getName() + " (Tier: " + tier + ")");
        } else {
            plugin.getDatabaseManager().removeClaimRecord(uuid, "booster");
            plugin.getDatabaseManager().removeClaimRecord(uuid, "booster_2");
            sender.sendMessage(ChatColor.GREEN + "Reset all claims for " + target.getName());
        }
    }

    private void handleStats(CommandSender sender, String[] args) {
        org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        java.util.UUID uuid = target.getUniqueId();
        String discordId = plugin.getDatabaseManager().getDiscordId(uuid);
        boolean isBoosting = plugin.getDatabaseManager().isBoosting(uuid);
        int boostCount = plugin.getDatabaseManager().getBoostCount(uuid);
        boolean claimed1 = plugin.getDatabaseManager().hasAlreadyClaimed(uuid, "booster");
        boolean claimed2 = plugin.getDatabaseManager().hasAlreadyClaimed(uuid, "booster_2");

        sender.sendMessage(
                ChatColor.GRAY + "--- Stats for " + ChatColor.WHITE + target.getName() + ChatColor.GRAY + " ---");
        sender.sendMessage(ChatColor.GRAY + "UUID: " + ChatColor.WHITE + uuid);
        sender.sendMessage(
                ChatColor.GRAY + "Discord ID: " + ChatColor.WHITE + (discordId != null ? discordId : "Not Linked"));
        sender.sendMessage(
                ChatColor.GRAY + "Is Boosting (DB): " + (isBoosting ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        sender.sendMessage(ChatColor.GRAY + "Boost Count (DB): " + ChatColor.WHITE + boostCount);
        sender.sendMessage(
                ChatColor.GRAY + "Claimed Tier 1: " + (claimed1 ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        sender.sendMessage(
                ChatColor.GRAY + "Claimed Tier 2: " + (claimed2 ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("help");
            completions.add("link");
            completions.add("claim");
            completions.add("unlink");

            if (sender.hasPermission("boosterrewards.admin")) {
                completions.add("reload");
                completions.add("admin");
                completions.add("stats");
                completions.add("resetclaim");
            }
            return filter(completions, args[0]);
        }
        if (args.length == 2 && ("stats".equalsIgnoreCase(args[0]) || "resetclaim".equalsIgnoreCase(args[0])
                || "unlink".equalsIgnoreCase(args[0]))) {
            return null; // Return null to show player lists
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        if (input == null || input.isEmpty())
            return list;
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
