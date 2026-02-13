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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("boosterrewards.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        new AdminGUI(plugin).open((Player) sender);
        return true;
    }
}
