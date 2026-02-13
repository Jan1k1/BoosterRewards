package studio.jan1k.boosterrewards.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.ItemSerializer;
import studio.jan1k.boosterrewards.database.DatabaseManager;
import studio.jan1k.boosterrewards.utils.SchedulerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;
    private final String tier;
    private final Map<Integer, Integer> slotToRewardId = new HashMap<>();
    private final Map<Integer, String> slotToItemHash = new HashMap<>();

    public ClaimGUI(BoosterReward plugin) {
        this.plugin = plugin;
        this.inventory = null;
        this.tier = null;
    }

    public ClaimGUI(BoosterReward plugin, Player player, String tier) {
        this.plugin = plugin;
        this.tier = tier;
        String title = tier.equals("booster_2") ? "Claim: Tier 2" : "Claim: Tier 1";
        this.inventory = studio.jan1k.boosterrewards.utils.SchedulerUtils.isFolia()
                ? org.bukkit.Bukkit.createInventory(this, 54, title)
                : org.bukkit.Bukkit.createInventory(this, 54, title);

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(ChatColor.RED + "You cannot claim rewards in Creative mode.");
            return;
        }

        if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
                player.getOpenInventory().getType() != InventoryType.CREATIVE) {
            player.closeInventory();
        }

        loadRewards(player);
    }

    private void loadRewards(Player player) {
        SchedulerUtils.runAsync(plugin, () -> {
            List<DatabaseManager.PendingReward> rewards = plugin.getDatabaseManager()
                    .getPendingRewards(player.getUniqueId());

            SchedulerUtils.runSync(plugin, () -> {
                if (!player.isOnline())
                    return;

                if (rewards.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You have no pending rewards.");
                    return;
                }

                List<DatabaseManager.PendingReward> filtered = new ArrayList<>();
                for (DatabaseManager.PendingReward reward : rewards) {
                    if (reward.getRewardType().equals(tier)) {
                        filtered.add(reward);
                    }
                }

                if (filtered.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You have no pending rewards for this tier.");
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                int slot = 0;

                for (DatabaseManager.PendingReward reward : filtered) {
                    if (slot >= 54)
                        break;

                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = mapper.readValue(reward.getRewardData(), Map.class);
                        ItemStack item = ItemSerializer.deserialize(itemMap);

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
                        if (reward.getItemHash() != null) {
                            slotToItemHash.put(slot, reward.getItemHash());
                        }
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClaimGUI))
            return;

        ClaimGUI gui = (ClaimGUI) event.getInventory().getHolder();

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null)
            return;

        if (clickedInventory.equals(event.getView().getTopInventory())) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (gui.slotToRewardId.containsKey(slot)) {
                claimReward(player, gui, slot);
            }
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    private void claimReward(Player player, ClaimGUI gui, int slot) {
        ItemStack item = gui.inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR)
            return;

        int rewardId = gui.slotToRewardId.get(slot);

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full!");
            return;
        }

        gui.slotToRewardId.remove(slot);
        gui.inventory.setItem(slot, null);

        SchedulerUtils.runAsync(gui.plugin, () -> {
            try {
                gui.plugin.getDatabaseManager().removePendingReward(rewardId);

                SchedulerUtils.runSync(gui.plugin, () -> {
                    if (player.isOnline()) {
                        ItemStack toGive = item.clone();
                        ItemMeta meta = toGive.getItemMeta();
                        if (meta != null && meta.hasLore()) {
                            List<String> lore = meta.getLore();
                            if (!lore.isEmpty() && lore.get(lore.size() - 1).contains("Click to Claim")) {
                                lore.remove(lore.size() - 1);
                                if (!lore.isEmpty() && lore.get(lore.size() - 1).trim().isEmpty()) {
                                    lore.remove(lore.size() - 1);
                                }
                                meta.setLore(lore);
                            }
                            toGive.setItemMeta(meta);
                        }
                        player.getInventory().addItem(toGive);

                        String hash = gui.slotToItemHash.get(slot);
                        if (hash != null) {
                            gui.plugin.getDatabaseManager().addItemClaimRecord(player.getUniqueId(), hash);
                        }

                        gui.plugin.getDatabaseManager().addClaimRecord(player.getUniqueId(), gui.tier);
                        player.sendMessage(ChatColor.GREEN + "Reward claimed!");
                    }
                });
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "An error occurred.");
                e.printStackTrace();
                SchedulerUtils.runSync(gui.plugin, () -> {
                    if (player.isOnline()) {
                        gui.inventory.setItem(slot, item);
                        gui.slotToRewardId.put(slot, rewardId);
                    }
                });
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ClaimGUI) {
            event.setCancelled(true);
        }
    }
}
