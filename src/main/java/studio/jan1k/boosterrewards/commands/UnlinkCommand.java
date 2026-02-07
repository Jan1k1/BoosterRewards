package studio.jan1k.boosterrewards.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;

public class UnlinkCommand implements CommandExecutor {

    private final BoosterReward plugin;

    public UnlinkCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getPlayerData(player.getUniqueId()) == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfigManager().getInGameMessage("logout.not-linked")));
                return;
            }

            plugin.getDatabaseManager().removeUser(player.getUniqueId());
            plugin.removeCachedPlayer(player.getUniqueId());

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getInGameMessage("logout.success")));
        });

        return true;
    }
}

