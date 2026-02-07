package studio.jan1k.boosterrewards.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.core.ItemSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminGUI implements Listener, InventoryHolder {

    private final BoosterReward plugin;
    private final Inventory inventory;
    private boolean isEditor = false;

    public AdminGUI(BoosterReward plugin, boolean editor) {
        this.plugin = plugin;
        this.isEditor = editor;
        if (editor) {
            this.inventory = Bukkit.createInventory(this, 54, "Drop Items to Add Rewards");
            loadRewards();
        } else {
            this.inventory = Bukkit.createInventory(this, 27, "BoosterReward Admin");
            setupMainMenu();
        }
    }

    private void setupMainMenu() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ItemStack editorBtn = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta em = editorBtn.getItemMeta();
        em.setDisplayName(ChatColor.AQUA + "Edit Booster Rewards");
        em.setLore(Arrays.asList(ChatColor.GRAY + "Click to manage items given on boost"));
        editorBtn.setItemMeta(em);
        inventory.setItem(13, editorBtn);
    }

    @SuppressWarnings("unchecked")
    private void loadRewards() {
        if (!plugin.getConfig().contains("rewards.booster.on-boost.items"))
            return;
        List<Map<?, ?>> items = plugin.getConfig().getMapList("rewards.booster.on-boost.items");
        for (Map<?, ?> map : items) {
            try {
                inventory.addItem(ItemSerializer.deserialize((Map<String, Object>) map));
            } catch (Exception e) {
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminGUI))
            return;
        AdminGUI gui = (AdminGUI) event.getInventory().getHolder();

        if (!gui.isEditor) {
            event.setCancelled(true);
            if (event.getSlot() == 13) {
                new AdminGUI(plugin, true).open((Player) event.getWhoClicked());
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminGUI))
            return;
        AdminGUI gui = (AdminGUI) event.getInventory().getHolder();

        if (gui.isEditor) {
            saveRewards(event.getInventory());
            plugin.reloadConfig();
            event.getPlayer().sendMessage(ChatColor.GREEN + "Rewards saved!");
        }
    }

    private void saveRewards(Inventory inv) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                list.add(ItemSerializer.serialize(item));
            }
        }
        plugin.getConfig().set("rewards.booster.on-boost.items", list);
        plugin.saveConfig();
    }
}

