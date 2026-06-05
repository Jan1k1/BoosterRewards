package studio.jan1k.boosterrewards;

import org.bukkit.Bukkit;
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
import studio.jan1k.boosterrewards.utils.SchedulerUtils;

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

        loadCoreModules();
        initializeDiscordBot();
    }

    public void initializeDiscordBot() {
        shutdownDiscordBot();

        String discordToken = configManager.getDiscordToken();
        if (discordToken == null || discordToken.equals("YOUR_BOT_TOKEN_HERE") || discordToken.isEmpty()) {
            Logs.warn("Discord Bot Token is not configured. Discord features will be disabled.");
            Logs.warn("Please set your bot token in discord.yml and reload.");
            return;
        }

        String guildId = configManager.getDiscordGuildId();
        if (guildId == null || guildId.equals("000000000000000000") || guildId.isEmpty()) {
            Logs.warn("Discord Guild ID is not configured. Discord features will be disabled.");
            Logs.warn("Please set your guild-id in discord.yml and reload.");
            return;
        }

        this.discordBot = new DiscordBot(this, linkManager);
    }

    public void shutdownDiscordBot() {
        if (this.discordBot != null) {
            this.discordBot.stop();
            this.discordBot = null;
        }
    }

    private void suppressLibraryLogs() {
        java.util.logging.Logger.getLogger("net.dv8tion.jda").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("com.zaxxer.hikari").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.h2").setLevel(java.util.logging.Level.OFF);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.net.dv8tion.jda", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.com.zaxxer.hikari", "warn");
    }

    private void loadCoreModules() {
        Logs.info("Initializing core modules...");
        this.databaseManager = new DatabaseManager(this);
        this.linkManager = new LinkManager();
        this.itemRewardHandler = new studio.jan1k.boosterrewards.core.ItemRewardHandler(this);
        this.rewardManager = new RewardManager(this);
        this.itemRewardHandler.refreshCache();

        registerCommands();

        long interval = getConfig().getLong("sync.interval", 300) * 20L;
        SchedulerUtils.runTimer(this, new RewardSyncTask(this)::run, 100L, interval);

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

        if (getConfig().getBoolean("features.metrics", true)) {
            try {
                new org.bstats.bukkit.Metrics(this, 29390);
            } catch (NoClassDefFoundError e) {
                Logs.warn("Metrics failed to initialize (Missing dependency). This is normal if using the Lite jar.");
            }
        }

        if (getConfig().getBoolean("features.update-checker", true)) {
            new studio.jan1k.boosterrewards.utils.UpdateChecker(this, 118476).check();
        }
    }

    private void registerCommands() {
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
    }

    @Override
    public void onDisable() {
        shutdownDiscordBot();
        if (databaseManager != null) {
            databaseManager.close();
        }
        Logs.info("Plugin disabled successfully.");
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
        Logs.raw("  §fBOOSTER   Discord Boost Rewards");
        Logs.raw("  §fjan1k.org");
        Logs.raw(" ");
        Logs.raw("  §eIssues and updates");
        Logs.raw("  §b→ https://github.com/Jan1k1/BoosterRewards");
        Logs.raw(" ");
    }

    private void verifyBoostersOnStartup() {
        if (discordBot == null || discordBot.getJDA() == null)
            return;

        Logs.info("Verifying active boosters status with Discord...");
        SchedulerUtils.runAsync(this, () -> {
            java.util.List<String> activeIds = databaseManager.getAllActiveBoosters();
            String guildId = configManager.getDiscordGuildId();
            net.dv8tion.jda.api.entities.Guild guild = discordBot.getJDA().getGuildById(guildId);

            if (guild == null)
                return;

            int offlineSyncs = 0;
            for (String discordId : activeIds) {
                try {
                    net.dv8tion.jda.api.entities.Member member = guild.retrieveMemberById(discordId).complete();
                    if (member == null)
                        continue;

                    boolean isBoosting = member.getTimeBoosted() != null;
                    int boostCount = (int) guild.getBoosters().stream().filter(m -> m.getId().equals(discordId))
                            .count();
                    if (isBoosting && boostCount == 0)
                        boostCount = 1;

                    UUID uuid = databaseManager.getUuid(discordId);
                    if (uuid != null) {
                        databaseManager.setBoosterStatus(uuid, isBoosting, boostCount);

                        if (isBoosting) {
                            org.bukkit.configuration.ConfigurationSection tiers = getConfig()
                                    .getConfigurationSection("rewards");
                            if (tiers != null) {
                                for (String tier : tiers.getKeys(false)) {
                                    if (getConfig().getBoolean("rewards." + tier + ".enabled", false)) {
                                        if (!databaseManager.hasAlreadyClaimed(uuid, tier)) {
                                            if (tier.equals("booster")
                                                    || (tier.equals("booster_2") && boostCount >= 2)) {
                                                rewardManager.giveReward(uuid, tier);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        offlineSyncs++;
                    }
                } catch (Exception ignored) {
                }
            }
            if (offlineSyncs > 0) {
                Logs.info("Verified " + offlineSyncs + " boosters from database.");
            }
        });
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
