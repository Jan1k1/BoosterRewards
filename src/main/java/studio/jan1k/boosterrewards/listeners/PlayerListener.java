package studio.jan1k.boosterrewards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import studio.jan1k.boosterrewards.BoosterReward;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final BoosterReward plugin;

    public PlayerListener(BoosterReward plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String discordId = plugin.getDatabaseManager().getDiscordId(uuid);
        if (discordId != null) {
            plugin.cachePlayer(uuid, discordId);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeCachedPlayer(event.getPlayer().getUniqueId());
    }
}

