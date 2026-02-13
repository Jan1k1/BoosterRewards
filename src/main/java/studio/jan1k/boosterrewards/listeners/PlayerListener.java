package studio.jan1k.boosterrewards.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import studio.jan1k.boosterrewards.BoosterReward;

public class PlayerListener implements Listener {

    private final BoosterReward plugin;

    public PlayerListener(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDatabaseManager().getDiscordId(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeCachedPlayer(event.getPlayer().getUniqueId());
    }
}
