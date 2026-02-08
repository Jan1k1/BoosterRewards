package studio.jan1k.boosterrewards.core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.Logs;

import java.util.List;
import java.util.Map;

public class ItemRewardHandler {

    private final BoosterReward plugin;

    public ItemRewardHandler(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void queueItemRewards(Player player, String tier) {
        String itemPath = "rewards." + tier + ".items";
        if (!plugin.getConfig().contains(itemPath)) {
            return;
        }

        List<Map<?, ?>> items = plugin.getConfig().getMapList(itemPath);
        for (Map<?, ?> map : items) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) map;

                // Add to pending rewards in database
                plugin.getDatabaseManager().addPendingReward(
                        player.getUniqueId(),
                        com.fasterxml.jackson.databind.util.JSONPObject.class != null ? // check if jackson is available
                                                                                        // (it is in our project)
                                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(itemMap) : "",
                        tier);
            } catch (Exception e) {
                Logs.error("Failed to queue reward item for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void giveItemRewards(Player player, String tier) {
        // Keeps for backward compatibility or direct gives if needed, but RewardManager
        // now uses queueItemRewards
        String itemPath = "rewards." + tier + ".items";
        if (!plugin.getConfig().contains(itemPath)) {
            return;
        }

        List<Map<?, ?>> items = plugin.getConfig().getMapList(itemPath);
        for (Map<?, ?> map : items) {
            try {
                @SuppressWarnings("unchecked")
                ItemStack item = ItemSerializer.deserialize((Map<String, Object>) map);
                player.getInventory().addItem(item);
            } catch (Exception e) {
                Logs.error("Failed to give reward item to " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}
