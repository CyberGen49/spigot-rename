package org.simplecyber.rename;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PreviewGui implements Listener {

    private ItemStack createItem(Material material, String name, String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Plugin.translateColors(name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private Inventory inv;
    private FileConfiguration config;
    private ItemStack btnCancel;
    private ItemStack btnConfirm;
    private Runnable confirm;
    public PreviewGui(JavaPlugin plugin, FileConfiguration conf, Player player, ItemStack item, Runnable onConfirm) {
        config = conf;
        confirm = onConfirm;
        btnCancel = createItem(Material.RED_STAINED_GLASS_PANE, config.getString("preview_gui.text.button_cancel"));
        btnConfirm = createItem(Material.LIME_STAINED_GLASS_PANE, config.getString("preview_gui.text.button_confirm"));
        inv = Bukkit.createInventory(null, 9, Plugin.translateColors(config.getString("preview_gui.text.title")));
        inv.setItem(0, btnCancel);
        inv.setItem(1, btnCancel);
        inv.setItem(2, btnCancel);
        inv.setItem(4, item);
        inv.setItem(6, btnConfirm);
        inv.setItem(7, btnConfirm);
        inv.setItem(8, btnConfirm);
        player.openInventory(inv);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        final ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;
        final Player player = (Player) e.getWhoClicked();
        if (item.equals(btnCancel)) {
            player.closeInventory();
        } else if (item.equals(btnConfirm)) {
            player.closeInventory();
            confirm.run();
        }
    }
    @EventHandler public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }

    @EventHandler public void onInventoryClose(InventoryCloseEvent e) {
        HandlerList.unregisterAll(this);
    }

}