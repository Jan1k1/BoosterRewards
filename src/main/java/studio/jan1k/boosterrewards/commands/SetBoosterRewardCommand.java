package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.gui.AdminGUI;

public class SetBoosterRewardCommand implements CommandExecutor {

    private final BoosterReward plugin;

    public SetBoosterRewardCommand(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boosterrewards.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                new AdminGUI(plugin).open(player);
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /setboosterreward");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Console cannot use the reward editor GUI.");
        }
        return true;
    }
}
