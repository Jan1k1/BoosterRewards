package studio.jan1k.boosterrewards.core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.utils.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class ItemRewardHandler {

    private final BoosterReward plugin;
    private final ObjectMapper mapper = new ObjectMapper();

    public ItemRewardHandler(BoosterReward plugin) {
        this.plugin = plugin;
    }

    public void queueItemRewards(Player player, String tier) {
        String itemPath = "rewards." + tier + ".items";
        List<?> items = plugin.getConfig().getList(itemPath);
        if (items == null)
            return;

        for (Object obj : items) {
            try {
                Map<String, Object> itemMap;
                if (obj instanceof ItemStack) {
                    itemMap = ItemSerializer.serialize((ItemStack) obj);
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    itemMap = map;
                    if (!itemMap.containsKey("==")) {
                        itemMap.put("==", "org.bukkit.inventory.ItemStack");
                    }
                } else {
                    continue;
                }

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
        String itemPath = "rewards." + tier + ".items";
        List<?> items = plugin.getConfig().getList(itemPath);
        if (items == null)
            return;

        for (Object obj : items) {
            try {
                ItemStack item;
                if (obj instanceof ItemStack) {
                    item = (ItemStack) obj;
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    item = ItemSerializer.deserialize(map);
                } else {
                    continue;
                }
                player.getInventory().addItem(item);
            } catch (Exception e) {
                Logs.error("Failed to give reward item to " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}
