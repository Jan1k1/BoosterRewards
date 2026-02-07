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
        }
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
            }
            return filter(completions, args[0]);
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
