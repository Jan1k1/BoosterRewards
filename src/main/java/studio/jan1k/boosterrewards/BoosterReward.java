package studio.jan1k.boosterrewards;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import studio.jan1k.boosterrewards.core.PlayerData;
import studio.jan1k.boosterrewards.core.RewardManager;
import studio.jan1k.boosterrewards.commands.ClaimCommand;
import studio.jan1k.boosterrewards.commands.EmptyTabCompleter;
import studio.jan1k.boosterrewards.commands.LinkCommand;
import studio.jan1k.boosterrewards.commands.MainCommand;
import studio.jan1k.boosterrewards.commands.UnlinkCommand;
import studio.jan1k.boosterrewards.commands.SetBoosterRewardCommand;
import studio.jan1k.boosterrewards.core.ConfigManager;
import studio.jan1k.boosterrewards.core.DependencyManager;
import studio.jan1k.boosterrewards.core.LinkManager;
import studio.jan1k.boosterrewards.database.DatabaseManager;
import studio.jan1k.boosterrewards.discord.DiscordBot;
import studio.jan1k.boosterrewards.gui.AdminGUI;
import studio.jan1k.boosterrewards.gui.ClaimGUI;
import studio.jan1k.boosterrewards.tasks.RewardSyncTask;
import studio.jan1k.boosterrewards.utils.Logs;

public class BoosterReward extends JavaPlugin {

    public static final String PREFIX = "&#FF69B4&lʙᴏᴏsᴛᴇʀ &8» &7";

    private static BoosterReward instance;
    private DiscordBot discordBot;
    private LinkManager linkManager;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private RewardManager rewardManager;
    private studio.jan1k.boosterrewards.core.ItemRewardHandler itemRewardHandler;
    private final java.util.Map<UUID, PlayerData> cache = new java.util.HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        suppressLibraryLogs();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        printBanner();
        Logs.info("Initializing BoosterRewards v" + getDescription().getVersion());

        new DependencyManager(this).load();

        configManager = new ConfigManager(this);
        saveDefaultConfig();
        configManager.loadFullConfigs();

        String discordToken = configManager.getDiscordToken();
        if (discordToken == null || discordToken.equals("YOUR_BOT_TOKEN_HERE") || discordToken.isEmpty()) {
            printLicenseError("DISCORD BOT TOKEN REQUIRED",
                    "A Discord bot token is required to run this plugin.",
                    "Please configure your bot token in discord.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String guildId = configManager.getDiscordGuildId();
        if (guildId == null || guildId.equals("000000000000000000") || guildId.isEmpty()) {
            printLicenseError("DISCORD GUILD ID REQUIRED",
                    "A Target Discord Server ID is required.",
                    "Please configure your guild-id in discord.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadModules();
    }

    private void suppressLibraryLogs() {
        // JUL silencing (for JDA and others)
        java.util.logging.Logger.getLogger("net.dv8tion.jda").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("com.zaxxer.hikari").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.h2").setLevel(java.util.logging.Level.OFF);

        // System properties for slf4j-simple (since we bundle it)
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.net.dv8tion.jda", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.com.zaxxer.hikari", "warn");
    }

    private void loadModules() {
        Logs.info("Initializing modules...");
        this.databaseManager = new DatabaseManager(this);
        this.linkManager = new LinkManager();
        this.itemRewardHandler = new studio.jan1k.boosterrewards.core.ItemRewardHandler(this);
        this.rewardManager = new RewardManager(this);
        this.discordBot = new DiscordBot(this, linkManager);

        getCommand("link").setExecutor(new LinkCommand(this));
        getCommand("link").setTabCompleter(new EmptyTabCompleter());

        getCommand("unlink").setExecutor(new UnlinkCommand(this));
        getCommand("unlink").setTabCompleter(new EmptyTabCompleter());

        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("claim").setTabCompleter(new EmptyTabCompleter());

        getCommand("setboosterreward").setExecutor(new SetBoosterRewardCommand(this));
        getCommand("setboosterreward").setTabCompleter(new EmptyTabCompleter());

        getCommand("forceunlink").setExecutor(new studio.jan1k.boosterrewards.commands.ForceUnlinkCommand(this));
        getCommand("forceunlink").setTabCompleter(new studio.jan1k.boosterrewards.commands.ForceUnlinkTabCompleter());

        MainCommand mainCmd = new MainCommand(this);
        getCommand("boosterrewards").setExecutor(mainCmd);
        getCommand("boosterrewards").setTabCompleter(mainCmd);

        new RewardSyncTask(this).runTaskTimerAsynchronously(this, 100L,
                getConfig().getLong("sync.interval", 300) * 20L);

        getServer().getPluginManager().registerEvents(new AdminGUI(this), this);
        getServer().getPluginManager().registerEvents(new ClaimGUI(this), this);
        getServer().getPluginManager().registerEvents(new studio.jan1k.boosterrewards.gui.ClaimSelectorGUI(this, null),
                this);
        getServer().getPluginManager().registerEvents(new studio.jan1k.boosterrewards.listeners.PlayerListener(this),
                this);

        if (getConfig().getBoolean("sync.verify-on-startup", true)) {
            verifyBoostersOnStartup();
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new studio.jan1k.boosterrewards.integrations.BoosterRewardsExpansion(this).register();
        }

        // Metrics
        if (getConfig().getBoolean("features.metrics", true)) {
            try {
                new org.bstats.bukkit.Metrics(this, 29390);
            } catch (NoClassDefFoundError e) {
                Logs.warn("Metrics failed to initialize (Missing dependency). This is normal if using the Lite jar.");
            }
        }

        // Update Checker
        if (getConfig().getBoolean("features.update-checker", true)) {
            new studio.jan1k.boosterrewards.utils.UpdateChecker(this);
        }

        // Module initialization complete
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.stop();
        }
    }

    private void printBanner() {
        String pink = "§x§E§3§5§4§F§F";
        String blue = "§x§5§4§A§5§F§F";
        String reset = "§r";

        Logs.raw(" ");
        Logs.bannerAccent(pink + " ██████╗  ██████╗  ██████╗ ███████╗████████╗███████╗██████╗ ");
        Logs.bannerAccent(pink + " ██╔══██╗██╔═══██╗██╔═══██╗██╔════╝╚══██╔══╝██╔════╝██╔══██╗");
        Logs.bannerAccent(pink + " ██████╔╝██║   ██║██║   ██║███████╗   ██║   █████╗  ██████╔╝");
        Logs.bannerAccent(pink + " ██╔══██╗██║   ██║██║   ██║╚════██║   ██║   ██╔══╝  ██╔══██╗");
        Logs.bannerAccent(pink + " ██████╔╝╚██████╔╝╚██████╔╝███████║   ██║   ███████╗██║  ██║");
        Logs.bannerAccent(pink + " ╚═════╝  ╚═════╝  ╚═════╝ ╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝");
        Logs.raw(" ");
        Logs.bannerAccent(blue + "  ██████╗ ███████╗██╗    ██╗ █████╗ ██████╗ ██████╗ ███████╗");
        Logs.bannerAccent(blue + "  ██╔══██╗██╔════╝██║    ██║██╔══██╗██╔══██╗██╔══██╗██╔════╝");
        Logs.bannerAccent(blue + "  ██████╔╝█████╗  ██║ █╗ ██║███████║██████╔╝██║  ██║███████╗");
        Logs.bannerAccent(blue + "  ██╔══██╗██╔══╝  ██║███╗██║██╔══██║██╔══██╗██║  ██║╚════██║");
        Logs.bannerAccent(blue + "  ██║  ██║███████╗╚███╔███╔╝██║  ██║██║  ██║██████╔╝███████║");
        Logs.bannerAccent(blue + "  ╚═╝  ╚═╝╚══════╝ ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝" + reset);
        Logs.raw(" ");
        Logs.raw("  §fBOOSTER   Premium Discord Boosting System");
        Logs.raw("  §f© 2026 jan1k.studio - All Rights Reserved");
        Logs.raw(" ");
        Logs.raw("  §e⚠ Found a bug or have a suggestion?");
        Logs.raw("  §b→ Join our Discord: §fhttps://discord.gg/38Ebj42e");
        Logs.raw(" ");
    }

    private void printLicenseError(String title, String reason, String details) {
        Logs.raw(" ");
        Logs.raw("  §8============================================================");
        Logs.raw("  §c" + title);
        Logs.raw("  §8============================================================");
        Logs.raw(" ");
        Logs.raw("  §f" + reason);
        Logs.raw("  §f" + details);
        Logs.raw(" ");
        Logs.raw("  §8------------------------------------------------------------");
        Logs.raw("  §fSUPPORT: Create a ticket at §bhttps://discord.gg/38Ebj42e");
        Logs.raw("  §8------------------------------------------------------------");
        Logs.raw(" ");
    }

    private void verifyBoostersOnStartup() {
        Logs.info("Verifying saved boosters...");
        int count = databaseManager.getActiveBoosterCount();
        if (count > 0) {
            Logs.database("Found " + count + " active boosters in database");
        }
    }

    public static BoosterReward getInstance() {
        return instance;
    }

    public LinkManager getLinkManager() {
        return linkManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public studio.jan1k.boosterrewards.core.ItemRewardHandler getItemRewardHandler() {
        return itemRewardHandler;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    public void cachePlayer(UUID uuid, String discordId) {
        cache.put(uuid, new PlayerData(uuid, discordId));
    }

    public void removeCachedPlayer(UUID uuid) {
        cache.remove(uuid);
    }
}
