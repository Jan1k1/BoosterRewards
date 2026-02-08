package studio.jan1k.boosterrewards.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.ItemSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;
    private final GuiMode mode;
    private final String rewardPath;

    public enum GuiMode {
        MAIN_MENU,
        REWARD_SELECTOR,
        EDITOR
    }

    public AdminGUI(BoosterReward plugin, GuiMode mode, String rewardPath) {
        this.plugin = plugin;
        this.mode = mode;
        this.rewardPath = rewardPath;

        if (mode == GuiMode.EDITOR) {
            String title = "Editing: " + (rewardPath.contains("booster_2") ? "Booster Lvl 2" : "Booster Lvl 1");
            this.inventory = Bukkit.createInventory(this, 54, title);
            loadRewards();
        } else if (mode == GuiMode.REWARD_SELECTOR) {
            this.inventory = Bukkit.createInventory(this, 27, "Select Reward Tier");
            setupSelectorMenu();
        } else {
            this.inventory = Bukkit.createInventory(this, 27, "BoosterReward Admin");
            setupMainMenu();
        }
    }

    // Default constructor for Main Menu
    public AdminGUI(BoosterReward plugin) {
        this(plugin, GuiMode.MAIN_MENU, null);
    }

    private void setupMainMenu() {
        fillGlass();

        // Button: Edit Rewards (Opens Selector)
        ItemStack rewardsBtn = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta rm = rewardsBtn.getItemMeta();
        rm.setDisplayName(ChatColor.AQUA + "Edit Rewards");
        rm.setLore(Arrays.asList(
                ChatColor.GRAY + "Configure items given to boosters",
                ChatColor.GRAY + "Supports multiple tiers"));
        rewardsBtn.setItemMeta(rm);
        inventory.setItem(11, rewardsBtn);

        // Button: Post Discord Panel
        ItemStack discordBtn = new ItemStack(Material.BOOK);
        ItemMeta dm = discordBtn.getItemMeta();
        dm.setDisplayName(ChatColor.LIGHT_PURPLE + "Post Link Panel");
        dm.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to post the Link Account embed",
                ChatColor.GRAY + "to the configured Discord channel."));
        discordBtn.setItemMeta(dm);
        inventory.setItem(13, discordBtn);

        // Button: Reload Config
        ItemStack reloadBtn = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta rlm = reloadBtn.getItemMeta();
        rlm.setDisplayName(ChatColor.RED + "Reload Config");
        rlm.setLore(Arrays.asList(ChatColor.GRAY + "Reloads all configuration files"));
        reloadBtn.setItemMeta(rlm);
        inventory.setItem(15, reloadBtn);
    }

    private void setupSelectorMenu() {
        fillGlass();

        // Tier 1
        ItemStack t1 = new ItemStack(Material.EMERALD);
        ItemMeta m1 = t1.getItemMeta();
        m1.setDisplayName(ChatColor.GREEN + "Booster Tier 1");
        m1.setLore(Arrays.asList(ChatColor.GRAY + "Standard Server Booster"));
        t1.setItemMeta(m1);
        inventory.setItem(11, t1);

        // Tier 2
        ItemStack t2 = new ItemStack(Material.NETHER_STAR);
        ItemMeta m2 = t2.getItemMeta();
        m2.setDisplayName(ChatColor.LIGHT_PURPLE + "Booster Tier 2");
        m2.setLore(Arrays.asList(
                ChatColor.GRAY + "Double Booster (Nitro)",
                ChatColor.GRAY + "Requires 'booster_2' in config"));
        t2.setItemMeta(m2);
        inventory.setItem(15, t2);

        // Back
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(ChatColor.RED + "Back to Main Menu");
        back.setItemMeta(bm);
        inventory.setItem(22, back);
    }

    private void fillGlass() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private void loadRewards() {
        if (rewardPath == null)
            return;

        String tier = rewardPath.contains("booster_2") ? "booster_2" : "booster";
        List<ItemStack> items = plugin.getItemRewardHandler().getCachedRewards(tier);

        for (ItemStack item : items) {
            inventory.addItem(item.clone());
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminGUI))
            return;
        AdminGUI gui = (AdminGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        if (gui.mode != GuiMode.EDITOR) {
            event.setCancelled(true);
            int slot = event.getSlot();

            if (gui.mode == GuiMode.MAIN_MENU) {
                if (slot == 11) { // Edit Rewards -> Selector
                    new AdminGUI(plugin, GuiMode.REWARD_SELECTOR, null).open(player);
                } else if (slot == 13) { // Post Panel
                    String channelId = plugin.getConfig().getString("panel.channel-id");
                    if (channelId == null || channelId.equals("000000000000000000")) {
                        player.sendMessage(ChatColor.RED + "Please configure panel.channel-id in config.yml first!");
                        player.closeInventory();
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Posting link panel to Discord...");
                    plugin.getDiscordBot().postLinkPanel(channelId, player);
                    player.closeInventory();
                } else if (slot == 15) { // Reload
                    plugin.reloadConfig();
                    plugin.getConfigManager().loadFullConfigs();
                    player.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                    player.closeInventory();
                }
            } else if (gui.mode == GuiMode.REWARD_SELECTOR) {
                if (slot == 11) { // Tier 1
                    new AdminGUI(plugin, GuiMode.EDITOR, "rewards.booster.items").open(player);
                } else if (slot == 15) { // Tier 2
                    new AdminGUI(plugin, GuiMode.EDITOR, "rewards.booster_2.items").open(player);
                } else if (slot == 22) { // Back
                    new AdminGUI(plugin).open(player);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminGUI))
            return;
        AdminGUI gui = (AdminGUI) event.getInventory().getHolder();

        // Save only if looking at an editor
        if (gui.mode == GuiMode.EDITOR && gui.rewardPath != null) {
            saveRewards(event.getInventory(), gui.rewardPath);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Rewards saved for "
                    + (gui.rewardPath.contains("booster_2") ? "Tier 2" : "Tier 1") + "!");
        }
    }

    private void saveRewards(Inventory inv, String path) {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                list.add(item.clone());
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getConfig().set(path, list);
            plugin.saveConfig();
            // Update cache immediately
            plugin.getItemRewardHandler().refreshCache();
        });
    }
}
