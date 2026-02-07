package studio.jan1k.boosterrewards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.gui.ClaimGUI;

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

        // Open GUI
        // The constructor handles loading data and opening inventory
        new ClaimGUI(plugin, player);

        return true;
    }
}

