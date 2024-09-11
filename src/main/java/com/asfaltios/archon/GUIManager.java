package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class GUIManager implements Listener {

    private Plugin plugin;

    public GUIManager(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Admin Control Panel");

        // Player Moderation
        gui.setItem(10, createMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Player Moderation", "Manage players: ban, mute, kick, freeze, etc."));

        // Server Management
        gui.setItem(12, createMenuItem(Material.COMMAND_BLOCK, ChatColor.GOLD + "Server Management", "Control server-wide settings: time, weather, etc."));

        // World Management
        gui.setItem(14, createMenuItem(Material.GRASS_BLOCK, ChatColor.BLUE + "World Management", "Manage world settings: time, weather, regions, etc."));

        // Player Tools
        gui.setItem(16, createMenuItem(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Player Tools", "View player stats, inventory, and more."));

        // Advanced Tools
        gui.setItem(22, createMenuItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Advanced Tools", "Use advanced admin tools for special tasks."));

        // Fill in the remaining slots for a better design
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", ""));
            }
        }

        player.openInventory(gui);
    }

    // Player Moderation GUI
    public void openPlayerModerationGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Player Moderation");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + onlinePlayer.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Click to manage this player.");
            meta.setLore(lore);
            playerHead.setItemMeta(meta);
            gui.addItem(playerHead);
        }

        gui.setItem(45, createMenuItem(Material.IRON_SWORD, ChatColor.RED + "Kick All Players", "Kick every player from the server."));
        gui.setItem(46, createMenuItem(Material.GOLDEN_APPLE, ChatColor.GREEN + "Heal All Players", "Heal every player on the server."));
        gui.setItem(47, createMenuItem(Material.FEATHER, ChatColor.AQUA + "Toggle Fly for All", "Enable/Disable fly mode for all players."));
        gui.setItem(48, createMenuItem(Material.BARRIER, ChatColor.RED + "Mute All Players", "Mute all players on the server."));
        gui.setItem(49, createMenuItem(Material.ICE, ChatColor.AQUA + "Freeze All Players", "Freeze all players in place."));
        gui.setItem(53, createMenuItem(Material.BARRIER, ChatColor.RED + "Back to Main Menu", "Click to return to the main menu."));

        player.openInventory(gui);
    }

    // Server Management GUI
    public void openServerManagementGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Server Management");

        // Time management
        gui.setItem(10, createMenuItem(Material.CLOCK, ChatColor.YELLOW + "Set Day", "Change time to day."));
        gui.setItem(11, createMenuItem(Material.REDSTONE, ChatColor.GRAY + "Set Night", "Change time to night."));

        // Weather management
        gui.setItem(12, createMenuItem(Material.WATER_BUCKET, ChatColor.AQUA + "Set Rain", "Change weather to rain."));
        gui.setItem(13, createMenuItem(Material.LAVA_BUCKET, ChatColor.YELLOW + "Set Clear Weather", "Set weather to sunny."));

        // Server-wide settings
        gui.setItem(14, createMenuItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Toggle PvP", "Enable/Disable PvP on the server."));
        gui.setItem(15, createMenuItem(Material.BARRIER, ChatColor.RED + "Toggle Mob Spawning", "Enable/Disable mob spawning."));
        gui.setItem(16, createMenuItem(Material.FLINT_AND_STEEL, ChatColor.RED + "Toggle Fire Spread", "Enable/Disable fire spread in the world."));

        // Global player actions
        gui.setItem(19, createMenuItem(Material.IRON_SWORD, ChatColor.RED + "Kick All Players", "Kick all players from the server."));
        gui.setItem(20, createMenuItem(Material.GOLDEN_APPLE, ChatColor.GREEN + "Heal All Players", "Heal every player."));
        gui.setItem(21, createMenuItem(Material.TOTEM_OF_UNDYING, ChatColor.YELLOW + "Give God Mode to All", "Enable god mode for all players."));

        gui.setItem(53, createMenuItem(Material.BARRIER, ChatColor.RED + "Back to Main Menu", "Click to return to the main menu."));
        player.openInventory(gui);
    }

    // Utility to create GUI items
    private ItemStack createMenuItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(description);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Admin Control Panel")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.equals(ChatColor.GREEN + "Player Moderation")) {
                openPlayerModerationGui(player);
            } else if (itemName.equals(ChatColor.GOLD + "Server Management")) {
                openServerManagementGui(player);
            } else if (itemName.equals(ChatColor.RED + "Back to Main Menu")) {
                openMainGui(player);
            }
        }
    }
}
