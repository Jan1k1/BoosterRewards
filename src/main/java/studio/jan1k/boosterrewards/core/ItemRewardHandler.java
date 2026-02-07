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

    public void giveItemRewards(Player player, String tier) {
        String itemPath = "rewards." + tier + ".on-boost.items";
        if (!plugin.getConfig().contains(itemPath)) {
            return;
        }

        List<Map<?, ?>> items = plugin.getConfig().getMapList(itemPath);
        for (Map<?, ?> map : items) {
            try {
                @SuppressWarnings("unchecked")
                ItemStack item = ItemSerializer.deserialize((Map<String, Object>) map);
                // logic to give item or drop if full could be added here
                player.getInventory().addItem(item);
            } catch (Exception e) {
                Logs.error("Failed to give reward item to " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}
