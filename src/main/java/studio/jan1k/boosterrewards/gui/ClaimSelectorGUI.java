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
import java.util.UUID;

public class ClaimSelectorGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;

    public ClaimSelectorGUI(BoosterReward plugin, Player player) {
        this.plugin = plugin;
        this.inventory = player != null ? Bukkit.createInventory(this, 27, "Select Reward Tier") : null;

        if (player != null) {
            setupMenu(player);
            player.openInventory(inventory);
        }
    }

    private void setupMenu(Player player) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        int boostCount = plugin.getDatabaseManager().getBoostCount(player.getUniqueId());

        // Tier 1: Standard Booster
        ItemStack tier1 = new ItemStack(Material.EMERALD);
        ItemMeta m1 = tier1.getItemMeta();
        m1.setDisplayName(ChatColor.GREEN + "Booster Tier 1");
        m1.setLore(Arrays.asList(
                ChatColor.GRAY + "Standard Server Booster",
                ChatColor.GRAY + "Status: "
                        + (boostCount >= 1 ? ChatColor.GREEN + "Unlocked" : ChatColor.RED + "Locked")));
        tier1.setItemMeta(m1);
        inventory.setItem(11, tier1);

        // Tier 2: Double Booster (VIP)
        ItemStack tier2;
        boolean hasTier2 = boostCount >= 2;
        boolean tier2Enabled = plugin.getConfig().getBoolean("rewards.booster_2.enabled", false);

        if (tier2Enabled) {
            tier2 = new ItemStack(hasTier2 ? Material.NETHER_STAR : Material.BARRIER);
            ItemMeta m2 = tier2.getItemMeta();
            m2.setDisplayName(ChatColor.LIGHT_PURPLE + "Booster Tier 2 (VIP)");
            m2.setLore(Arrays.asList(
                    ChatColor.GRAY + "Double Booster (2x Boost)",
                    ChatColor.GRAY + "Status: "
                            + (hasTier2 ? ChatColor.GREEN + "Unlocked" : ChatColor.RED + "Requires 2 boosts")));
            tier2.setItemMeta(m2);
            inventory.setItem(15, tier2);
        }
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
        int slot = event.getSlot();

        int boostCount = plugin.getDatabaseManager().getBoostCount(player.getUniqueId());

        if (slot == 11) { // Tier 1
            if (boostCount >= 1) {
                player.closeInventory();
                new ClaimGUI(plugin, player, "booster");
            } else {
                player.sendMessage(ChatColor.RED + "You need at least 1 boost to claim this tier!");
            }
        } else if (slot == 15) { // Tier 2
            if (boostCount >= 2) {
                player.closeInventory();
                new ClaimGUI(plugin, player, "booster_2");
            } else {
                player.sendMessage(ChatColor.RED + "You need at least 2 boosts to claim VIP rewards!");
            }
        }
    }
}
