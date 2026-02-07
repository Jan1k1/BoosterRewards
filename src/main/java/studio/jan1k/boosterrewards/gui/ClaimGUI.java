package studio.jan1k.boosterrewards.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.ItemSerializer;
import studio.jan1k.boosterrewards.database.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;
    private final UUID ownerId;
    private final Map<Integer, Integer> slotToRewardId = new HashMap<>();

    // Constructor for Listener registration
    public ClaimGUI(BoosterReward plugin) {
        this.plugin = plugin;
        this.inventory = null;
        this.ownerId = null;
    }

    // Constructor for opening GUI
    public ClaimGUI(BoosterReward plugin, Player player) {
        this.plugin = plugin;
        this.ownerId = player.getUniqueId();
        this.inventory = Bukkit.createInventory(this, 54, "Claim Rewards");

        // Anti-Dupe / Safety Checks
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(ChatColor.RED + "You cannot claim rewards in Creative mode.");
            return;
        }

        // Check if player has another inventory open (Trade, etc)
        // Opening a new inventory automatically closes the previous one in Bukkit,
        // so we just need to ensure we don't carry over cursors/items exploits.
        // But to be safe per user request:
        if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
                player.getOpenInventory().getType() != InventoryType.CREATIVE) {
            player.closeInventory();
        }

        loadRewards(player);
    }

    private void loadRewards(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<DatabaseManager.PendingReward> rewards = plugin.getDatabaseManager()
                    .getPendingRewards(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline())
                    return;

                if (rewards.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You have no pending rewards.");
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                int slot = 0;

                for (DatabaseManager.PendingReward reward : rewards) {
                    if (slot >= 54)
                        break; // GUI full

                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = mapper.readValue(reward.getRewardData(), Map.class);
                        ItemStack item = ItemSerializer.deserialize(itemMap);

                        // Add some lore to indicate it's claimable
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                            lore.add(" ");
                            lore.add(ChatColor.GREEN + "➤ Click to Claim");
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }

                        inventory.setItem(slot, item);
                        slotToRewardId.put(slot, reward.getId());
                        slot++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                player.openInventory(inventory);
            });
        });
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /*
     * LISTENER EVENTS
     */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClaimGUI))
            return;

        ClaimGUI gui = (ClaimGUI) event.getInventory().getHolder();
        event.setCancelled(true); // Prevent picking up items

        if (event.getClickedInventory() != event.getView().getTopInventory())
            return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (gui.slotToRewardId.containsKey(slot)) {
            int rewardId = gui.slotToRewardId.get(slot);
            ItemStack item = event.getCurrentItem();

            if (item == null || item.getType() == Material.AIR)
                return;

            // Check for space
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.RED + "Your inventory is full!");
                return;
            }

            // Lock slot to prevent multiple clicks
            gui.slotToRewardId.remove(slot);
            event.getInventory().setItem(slot, null);

            // Remove from DB (Async)
            Bukkit.getScheduler().runTaskAsynchronously(gui.plugin, () -> {
                try {
                    // We attempt to remove it first. If successful, we give it in-game.
                    // This is the secure way to prevent dupes.
                    gui.plugin.getDatabaseManager().removePendingReward(rewardId);

                    // Give Item on Main Thread
                    Bukkit.getScheduler().runTask(gui.plugin, () -> {
                        if (player.isOnline()) {
                            // Remove "Click to Claim" lore before giving
                            ItemStack toGive = item.clone();
                            ItemMeta meta = toGive.getItemMeta();
                            if (meta != null && meta.hasLore()) {
                                List<String> lore = meta.getLore();
                                if (lore.size() >= 2 && lore.get(lore.size() - 1).contains("Click to Claim")) {
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    meta.setLore(lore);
                                }
                                toGive.setItemMeta(meta);
                            }
                            player.getInventory().addItem(toGive);
                            player.sendMessage(ChatColor.GREEN + "Reward claimed!");
                        }
                    });
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "An error occurred while claiming your reward.");
                    e.printStackTrace();
                    // Restore item if DB fails? (Ideally DB shouldn't fail, but let's be safe)
                    Bukkit.getScheduler().runTask(gui.plugin, () -> {
                        if (player.isOnline()) {
                            event.getInventory().setItem(slot, item);
                            gui.slotToRewardId.put(slot, rewardId);
                        }
                    });
                }
            });
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ClaimGUI) {
            event.setCancelled(true);
        }
    }
}
