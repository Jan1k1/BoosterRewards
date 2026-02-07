package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;

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

        // 1. Check if linked
        String discordId = plugin.getDatabaseManager().getDiscordId(player.getUniqueId());
        if (discordId == null) {
            player.sendMessage(ChatColor.RED + "❌ You must link your account before you can claim rewards!");
            player.sendMessage(ChatColor.GRAY + "Use /link to connect your Discord account.");
            return true;
        }

        // 2. Check if boosting
        if (!plugin.getDatabaseManager().isBoosting(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "❌ You are not currently boosting the server!");
            player.sendMessage(ChatColor.GRAY + "Boost our Discord server to unlock rewards!");
            return true;
        }

        new studio.jan1k.boosterrewards.gui.ClaimSelectorGUI(plugin, player);

        return true;
    }
}
