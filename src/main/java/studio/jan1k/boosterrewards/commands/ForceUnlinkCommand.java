package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import studio.jan1k.boosterrewards.BoosterReward;

import java.util.UUID;

public class ForceUnlinkCommand implements CommandExecutor {

    private final BoosterReward plugin;

    public ForceUnlinkCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boosterrewards.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /forceunlink <player>");
            return true;
        }

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);

        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        String discordId = plugin.getDatabaseManager().getDiscordId(uuid);

        if (discordId == null) {
            sender.sendMessage(ChatColor.RED + targetName + " is not linked.");
            return true;
        }

        plugin.getDatabaseManager().removeUser(uuid);
        plugin.removeCachedPlayer(uuid);
        sender.sendMessage(ChatColor.GREEN + "Successfully unlinked " + targetName + ".");

        if (target.isOnline()) {
            ((org.bukkit.entity.Player) target)
                    .sendMessage(ChatColor.RED + "Your account has been unlinked by an administrator.");
        }

        return true;
    }
}
