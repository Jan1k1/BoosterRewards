package studio.jan1k.boosterrewards.core;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemRewardHandler {

    private final BoosterReward plugin;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, List<ItemStack>> rewardCache = new HashMap<>();

    public ItemRewardHandler(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void refreshCache() {
        rewardCache.clear();
        loadTierToCache("booster");
        loadTierToCache("booster_2");
        // Support custom tiers if added in future
        if (plugin.getConfig().getConfigurationSection("rewards") != null) {
            for (String tier : plugin.getConfig().getConfigurationSection("rewards").getKeys(false)) {
                if (!rewardCache.containsKey(tier)) {
                    loadTierToCache(tier);
                }
            }
        }
        Logs.info("Item reward cache refreshed.");
    }

    private void loadTierToCache(String tier) {
        String itemPath = "rewards." + tier + ".items";
        List<?> items = plugin.getConfig().getList(itemPath);
        if (items == null) {
            rewardCache.put(tier, new ArrayList<>());
            return;
        }

        List<ItemStack> cachedItems = new ArrayList<>();
        for (Object obj : items) {
            try {
                if (obj instanceof ItemStack) {
                    cachedItems.add((ItemStack) obj);
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    cachedItems.add(ItemSerializer.deserialize(map));
                }
            } catch (Exception e) {
                Logs.error("Failed to cache reward item for tier " + tier + ": " + e.getMessage());
            }
        }
        rewardCache.put(tier, cachedItems);
    }

    public List<ItemStack> getCachedRewards(String tier) {
        return rewardCache.getOrDefault(tier, Collections.emptyList());
    }

    public void queueItemRewards(Player player, String tier) {
        List<ItemStack> items = getCachedRewards(tier);
        if (items.isEmpty())
            return;

        for (ItemStack item : items) {
            try {
                Map<String, Object> itemMap = ItemSerializer.serialize(item);
                plugin.getDatabaseManager().addPendingReward(
                        player.getUniqueId(),
                        mapper.writeValueAsString(itemMap),
                        tier);
            } catch (Exception e) {
                Logs.error("Failed to queue reward item for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void giveItemRewards(Player player, String tier) {
        List<ItemStack> items = getCachedRewards(tier);
        for (ItemStack item : items) {
            player.getInventory().addItem(item.clone());
        }
    }
}
