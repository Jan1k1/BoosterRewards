package studio.jan1k.boosterrewards.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {

    public static Map<String, Object> serialize(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return new HashMap<>();
        }
        Map<String, Object> serialized = item.serialize();
        if (!serialized.containsKey("==")) {
            serialized.put("==", "org.bukkit.inventory.ItemStack");
        }
        return serialized;
    }

    public static ItemStack deserialize(Map<String, Object> map) {
        try {
            if (map == null || map.isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            return ItemStack.deserialize(map);
        } catch (Exception e) {
            return new ItemStack(Material.STONE);
        }
    }
}
