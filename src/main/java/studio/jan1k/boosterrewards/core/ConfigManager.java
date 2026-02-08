package studio.jan1k.boosterrewards.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.Logs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private final BoosterReward plugin;
    private FileConfiguration discordConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void loadFullConfigs() {
        File discordFile = new File(plugin.getDataFolder(), "discord.yml");
        if (!discordFile.exists()) {
            saveResource("discord.yml");
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        plugin.reloadConfig();
        loadFullConfigs();
        Logs.success("Configuration reloaded successfully!");
    }

    private void saveResource(String resourceName) {
        try (InputStream in = plugin.getResource(resourceName)) {
            if (in != null) {
                File outFile = new File(plugin.getDataFolder(), resourceName);
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                Files.copy(in, outFile.toPath());
            }
        } catch (Exception e) {
            Logs.error("Could not save " + resourceName);
        }
    }

    public String getDiscordToken() {
        return discordConfig.getString("bot.token", "YOUR_BOT_TOKEN_HERE");
    }

    public String getDiscordGuildId() {
        return discordConfig.getString("bot.guild-id", "000000000000000000");
    }

    public String getBoostAnnouncementsChannel() {
        return discordConfig.getString("channels.boost-announcements", "000000000000000000");
    }

    public String getLinkLogsChannel() {
        return discordConfig.getString("channels.link-logs", "000000000000000000");
    }

    public String getPrefix() {
        return messagesConfig.getString("prefix", "&7[BoosterRewards] ");
    }

    public String getInGameMessage(String key) {
        return messagesConfig.getString("in-game." + key, "");
    }

    public String getDiscordMessage(String key) {
        return messagesConfig.getString("discord." + key, "");
    }

    public String getEmbedSetting(String type, String key) {
        return messagesConfig.getString("embeds." + type + "." + key, "");
    }

    public String getPanelSetting(String key) {
        return messagesConfig.getString("panel." + key, "");
    }

    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
