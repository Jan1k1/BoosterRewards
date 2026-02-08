package studio.jan1k.boosterrewards.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.jan1k.boosterrewards.BoosterReward;

import java.util.Arrays;

public class ClaimSelectorGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;
    private final int boostCount;

    public ClaimSelectorGUI(BoosterReward plugin, Player player) {
        this(plugin, player, (player != null && plugin.getDatabaseManager() != null)
                ? plugin.getDatabaseManager().getBoostCount(player.getUniqueId())
                : 0);
    }

    public ClaimSelectorGUI(BoosterReward plugin, Player player, int boostCount) {
        this.plugin = plugin;
        this.boostCount = boostCount;
        this.inventory = player != null ? Bukkit.createInventory(this, 27, "Select Reward Tier") : null;

        if (player != null && inventory != null) {
            setupMenu(player);
            player.openInventory(inventory);
        }
    }

    public int getBoostCount() {
        return boostCount;
    }

    private void setupMenu(Player player) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        boolean tier2Enabled = plugin.getConfig().getBoolean("rewards.booster_2.enabled", false);

        if (tier2Enabled) {
            addTier1(boostCount, 11);
            addTier2(boostCount, 15);
        } else {
            addTier1(boostCount, 13);
        }
    }

    private void addTier1(int boostCount, int slot) {
        ItemStack tier1 = new ItemStack(Material.EMERALD);
        ItemMeta m1 = tier1.getItemMeta();
        m1.setDisplayName(ChatColor.GREEN + "Booster Tier 1");
        m1.setLore(Arrays.asList(
                ChatColor.GRAY + "Standard Server Booster",
                ChatColor.GRAY + "Status: "
                        + (boostCount >= 1 ? ChatColor.GREEN + "Unlocked" : ChatColor.RED + "Locked")));
        tier1.setItemMeta(m1);
        inventory.setItem(slot, tier1);
    }

    private void addTier2(int boostCount, int slot) {
        boolean hasTier2 = boostCount >= 2;
        ItemStack tier2 = new ItemStack(hasTier2 ? Material.NETHER_STAR : Material.BARRIER);
        ItemMeta m2 = tier2.getItemMeta();
        m2.setDisplayName(ChatColor.LIGHT_PURPLE + "Booster Tier 2 (VIP)");
        m2.setLore(Arrays.asList(
                ChatColor.GRAY + "Double Booster (2x Boost)",
                ChatColor.GRAY + "Status: "
                        + (hasTier2 ? ChatColor.GREEN + "Unlocked" : ChatColor.RED + "Requires 2 boosts")));
        tier2.setItemMeta(m2);
        inventory.setItem(slot, tier2);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClaimSelectorGUI))
            return;

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory())
            return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null)
            return;

        ClaimSelectorGUI gui = (ClaimSelectorGUI) event.getInventory().getHolder();
        int currentBoostCount = gui.getBoostCount();
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Booster Tier 1")) {
            if (currentBoostCount >= 1) {
                player.closeInventory();
                new ClaimGUI(plugin, player, "booster");
            } else {
                player.sendMessage(ChatColor.RED + "You need at least 1 boost to claim this tier!");
            }
        } else if (name.contains("Booster Tier 2")) {
            if (currentBoostCount >= 2) {
                player.closeInventory();
                new ClaimGUI(plugin, player, "booster_2");
            } else {
                player.sendMessage(ChatColor.RED + "You need at least 2 boosts to claim VIP rewards!");
            }
        }
    }
}
