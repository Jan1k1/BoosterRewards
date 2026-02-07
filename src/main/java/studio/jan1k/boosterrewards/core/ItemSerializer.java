package studio.jan1k.boosterrewards.core;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSerializer {

    public static Map<String, Object> serialize(ItemStack item) {
        Map<String, Object> map = new HashMap<>();
        map.put("material", item.getType().name());
        map.put("amount", item.getAmount());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                map.put("name", meta.getDisplayName().replace("§", "&"));
            }
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.replaceAll(l -> l.replace("§", "&"));
                map.put("lore", lore);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(Map<String, Object> map) {
        try {
            String matName = (String) map.get("material");
            int amount = (Integer) map.get("amount");
            Material mat = Material.matchMaterial(matName);
            if (mat == null)
                return new ItemStack(Material.STONE);

            ItemStack item = new ItemStack(mat, amount);
            ItemMeta meta = item.getItemMeta();

            if (map.containsKey("name")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) map.get("name")));
            }
            if (map.containsKey("lore")) {
                List<String> lore = (List<String>) map.get("lore");
                List<String> coloredLore = new ArrayList<>();
                for (String l : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            return new ItemStack(Material.STONE);
        }
    }
}

