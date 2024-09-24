package com.asfaltios.archon;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class AdminGUI implements Listener {

    private static Archon plugin = Archon.getInstance();

    // Permissions
    private static final String PERM_PLAYERMANAGEMENT = "archon.admin.playermanagement";
    private static final String PERM_SERVERMANAGEMENT = "archon.admin.servermanagement";
    private static final String PERM_PERSONALTOOLS = "archon.admin.personaltools";

    // Sub-permissions
    private static final String PERM_PLAYERMANAGEMENT_KICK = "archon.admin.playermanagement.kick";
    private static final String PERM_PLAYERMANAGEMENT_BAN = "archon.admin.playermanagement.ban";
    private static final String PERM_PLAYERMANAGEMENT_MUTE = "archon.admin.playermanagement.mute";
    private static final String PERM_PLAYERMANAGEMENT_FREEZE = "archon.admin.playermanagement.freeze";
    private static final String PERM_PLAYERMANAGEMENT_TELEPORTTO = "archon.admin.playermanagement.teleportto";
    private static final String PERM_PLAYERMANAGEMENT_BRING = "archon.admin.playermanagement.bring";
    private static final String PERM_PLAYERMANAGEMENT_INSPECT = "archon.admin.playermanagement.inspect";
    private static final String PERM_PLAYERMANAGEMENT_PERMISSION = "archon.admin.playermanagement.permission";
    private static final String PERM_PLAYERMANAGEMENT_MESSAGE = "archon.admin.playermanagement.message";
    private static final String PERM_PLAYERMANAGEMENT_HEAL = "archon.admin.playermanagement.heal";
    private static final String PERM_PLAYERMANAGEMENT_FEED = "archon.admin.playermanagement.feed";
    private static final String PERM_PLAYERMANAGEMENT_SETHEALTH = "archon.admin.playermanagement.sethealth";
    private static final String PERM_SERVERMANAGEMENT_MAINTENANCE = "archon.admin.servermanagement.maintenance";
    private static final String PERM_SERVERMANAGEMENT_STOPSERVER = "archon.admin.servermanagement.stopserver";

    private static final String PERM_SERVERMANAGEMENT_CHANGETIME = "archon.admin.servermanagement.changetime";
    private static final String PERM_SERVERMANAGEMENT_CHANGEWEATHER = "archon.admin.servermanagement.changeweather";
    private static final String PERM_SERVERMANAGEMENT_MANAGEWORLDS = "archon.admin.servermanagement.manageworlds";
    private static final String PERM_SERVERMANAGEMENT_SERVERSTATS = "archon.admin.servermanagement.serverstats";
    private static final String PERM_SERVERMANAGEMENT_EXECUTECOMMAND = "archon.admin.servermanagement.executecommand";
    private static final String PERM_SERVERMANAGEMENT_MANAGEPLUGINS = "archon.admin.servermanagement.manageplugins";
    private static final String PERM_SERVERMANAGEMENT_WHITELIST = "archon.admin.servermanagement.whitelist";
    private static final String PERM_SERVERMANAGEMENT_VIEWLOGS = "archon.admin.servermanagement.viewlogs"; // Added this line

    private static final String PERM_PERSONALTOOLS_TOGGLEFLY = "archon.admin.personaltools.togglefly";
    private static final String PERM_PERSONALTOOLS_GODMODE = "archon.admin.personaltools.godmode";
    private static final String PERM_PERSONALTOOLS_HEAL = "archon.admin.personaltools.heal";
    private static final String PERM_PERSONALTOOLS_GIVEITEM = "archon.admin.personaltools.giveitem";
    private static final String PERM_PERSONALTOOLS_ENDERCHEST = "archon.admin.personaltools.enderchest";
    private static final String PERM_PERSONALTOOLS_TOGGLEGAMEMODE = "archon.admin.personaltools.togglegamemode";
    private static final String PERM_PERSONALTOOLS_VANISH = "archon.admin.personaltools.vanish";
    private static final String PERM_PERSONALTOOLS_SPEED = "archon.admin.personaltools.speed";

    private static boolean maintenanceMode = false;

    // Mute and Freeze Lists
    private static final Map<UUID, Boolean> muteList = new HashMap<>();
    private static final Map<UUID, Boolean> freezeList = new HashMap<>();
    private static final Map<UUID, Boolean> godModeList = new HashMap<>();
    private static final Map<UUID, Boolean> vanishList = new HashMap<>();

    // Pagination for Item GUI
    private static final Map<UUID, Integer> itemGUIPages = new HashMap<>();

    // GUI Customization Settings
    private static final Map<UUID, Material> playerGlassColor = new HashMap<>();
    private static final Material DEFAULT_GLASS_MATERIAL = Material.LIGHT_BLUE_STAINED_GLASS_PANE;

    private static final String PERMISSION_GUI_TITLE = ChatColor.DARK_PURPLE + "Permission Management"; // Added this line
    private static final String SET_HEALTH_GUI_TITLE = ChatColor.RED + "Set Player Health";

    /**
     * Opens the main admin GUI for the player.
     *
     * @param player The player to open the GUI for.
     */
    public static void openMainGUI(Player player) {
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.main-gui", "&bArchon Admin Panel"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Get player's preferred glass color or use default from config
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative borders with symmetrical pattern
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        gui.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(36, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(44, createGuiItem(Material.SEA_LANTERN, " "));

        // Centered title with beacon icon
        gui.setItem(4, createGuiItem(Material.BEACON, ChatColor.AQUA + "" + ChatColor.BOLD + "Archon Admin Panel"));

        // Online players display
        String onlinePlayersText = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.online-players", "&eOnline Players: &a%online%"))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        gui.setItem(13, createGuiItem(Material.ENDER_EYE, onlinePlayersText));

        // Section buttons with symmetrical placement
        gui.setItem(20, createGuiItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Player Management", ChatColor.GRAY + "Manage players"));
        gui.setItem(22, createGuiItem(Material.COMMAND_BLOCK, ChatColor.BLUE + "Server Management", ChatColor.GRAY + "Manage server settings"));
        gui.setItem(24, createGuiItem(Material.NETHERITE_AXE, ChatColor.GOLD + "Personal Tools", ChatColor.GRAY + "Access personal admin tools"));

        // Admin Settings and Customize GUI buttons
        gui.setItem(30, createGuiItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Admin Settings", ChatColor.GRAY + "Configure admin settings"));
        gui.setItem(32, createGuiItem(Material.PAINTING, ChatColor.LIGHT_PURPLE + "Customize GUI", ChatColor.GRAY + "Change GUI appearance"));

        // Close button
        gui.setItem(40, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        // Open GUI for the player with sound and particles
        player.openInventory(gui);

        // Play sound if enabled
        if (plugin.getConfig().getBoolean("gui.gui-open-sound.enabled", true)) {
            String soundName = plugin.getConfig().getString("gui.gui-open-sound.sound", "UI_BUTTON_CLICK");
            Sound sound = Sound.valueOf(soundName);
            float volume = (float) plugin.getConfig().getDouble("gui.gui-open-sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("gui.gui-open-sound.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }

        // Spawn particles if enabled
        if (plugin.getConfig().getBoolean("gui.gui-open-particles.enabled", true)) {
            String particleName = plugin.getConfig().getString("gui.gui-open-particles.particle", "PORTAL");
            Particle particle = Particle.valueOf(particleName);
            int count = plugin.getConfig().getInt("gui.gui-open-particles.count", 50);
            double offsetX = plugin.getConfig().getDouble("gui.gui-open-particles.offset-x", 1);
            double offsetY = plugin.getConfig().getDouble("gui.gui-open-particles.offset-y", 1);
            double offsetZ = plugin.getConfig().getDouble("gui.gui-open-particles.offset-z", 1);
            player.spawnParticle(particle, player.getLocation().add(0, 1, 0), count, offsetX, offsetY, offsetZ);
        }
    }





    /**
     * Creates an ItemStack with the given material, name, and lore.
     *
     * @param material The material of the item.
     * @param name     The display name of the item.
     * @param lore     The lore of the item.
     * @return The created ItemStack.
     */
    private static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null && lore.length > 0) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }



    /**
     * Handles clicks in the main admin GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onMainGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.main-gui", "&bArchon Admin Panel"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Prevent interaction with borders and decorations
        Material clickedType = clickedItem.getType();

        // Get player's preferred glass color or use default from config
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN || clickedType == Material.BEACON) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Player Management":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to access Player Management.");
                    return;
                }
                player.closeInventory();
                openPlayerManagementGUI(player);
                break;
            case "Server Management":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to access Server Management.");
                    return;
                }
                player.closeInventory();
                openServerManagementGUI(player);
                break;
            case "Personal Tools":
                if (!player.hasPermission(PERM_PERSONALTOOLS)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to access Personal Tools.");
                    return;
                }
                player.closeInventory();
                openPersonalToolsGUI(player);
                break;
            case "Admin Settings":
                player.closeInventory();
                openAdminSettingsGUI(player);
                break;
            case "Customize GUI":
                player.closeInventory();
                openCustomizeGUI(player);
                break;
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }



    private void openCustomizeGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.customize-gui", "&bCustomize GUI"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Decorative borders
        ItemStack borderItem = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Glass color options
        Material[] glassColors = {
                Material.WHITE_STAINED_GLASS_PANE,
                Material.ORANGE_STAINED_GLASS_PANE,
                Material.MAGENTA_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.YELLOW_STAINED_GLASS_PANE,
                Material.LIME_STAINED_GLASS_PANE,
                Material.PINK_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE,
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Material.CYAN_STAINED_GLASS_PANE,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.BLUE_STAINED_GLASS_PANE,
                Material.BROWN_STAINED_GLASS_PANE,
                Material.GREEN_STAINED_GLASS_PANE,
                Material.RED_STAINED_GLASS_PANE,
                Material.BLACK_STAINED_GLASS_PANE
        };

        int[] colorSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
        for (int i = 0; i < glassColors.length && i < colorSlots.length; i++) {
            Material glass = glassColors[i];
            String colorName = glass.name().replace("_STAINED_GLASS_PANE", "").replace("_", " ");
            ItemStack glassItem = createGuiItem(glass, ChatColor.WHITE + "Select Color", ChatColor.GRAY + "Click to select " + colorName.toLowerCase() + " color");
            gui.setItem(colorSlots[i], glassItem);
        }

        // Back and Close buttons
        gui.setItem(36, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(40, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    @EventHandler
    public void onCustomizeGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.customize-gui", "&bCustomize GUI"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (clickedType == Material.BLACK_STAINED_GLASS_PANE) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }

        if (itemName.equals("Back")) {
            player.closeInventory();
            openMainGUI(player);
            return;
        } else if (itemName.equals("Close")) {
            player.closeInventory();
            return;
        }

        // Update player's preferred glass color
        if (clickedType.toString().endsWith("_STAINED_GLASS_PANE")) {
            playerGlassColor.put(player.getUniqueId(), clickedType);
            player.sendMessage(ChatColor.GREEN + "GUI glass color changed to " + clickedType.name().replace("_STAINED_GLASS_PANE", "").replace("_", " ") + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.closeInventory();
            openMainGUI(player);
        }
    }




    /**
     * Opens the Admin Settings GUI.
     *
     * @param player The admin player.
     */
    private void openAdminSettingsGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.admin-settings-gui", "&bAdmin Settings"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative Border
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        gui.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(36, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(44, createGuiItem(Material.SEA_LANTERN, " "));

        // Title in the center top
        gui.setItem(4, createGuiItem(Material.NETHER_STAR, ChatColor.AQUA + "" + ChatColor.BOLD + "Admin Settings"));

        // Center area (slots 20-24,29-33)
        int[] itemSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        int index = 0;

        // Admin Settings Items
        List<ItemStack> items = new ArrayList<>();

        if (player.hasPermission(PERM_SERVERMANAGEMENT_VIEWLOGS)) {
            items.add(createGuiItem(Material.PAPER, ChatColor.YELLOW + "View Logs", "View server logs"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_WHITELIST)) {
            items.add(createGuiItem(Material.NAME_TAG, ChatColor.LIGHT_PURPLE + "Manage Whitelist", "Add or remove players from whitelist"));
            items.add(createGuiItem(Material.LEVER, ChatColor.GREEN + "Toggle Whitelist", "Enable or disable the whitelist"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_MAINTENANCE)) {
            items.add(createGuiItem(Material.REDSTONE_TORCH, ChatColor.RED + "Maintenance Mode", "Enable or disable maintenance mode"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_STOPSERVER)) {
            items.add(createGuiItem(Material.BARRIER, ChatColor.DARK_RED + "Stop Server", "Stop the server"));
        }

        // Place items in GUI
        for (ItemStack item : items) {
            if (index < itemSlots.length) {
                int slot = itemSlots[index];
                gui.setItem(slot, item);
                index++;
            } else {
                break; // No more slots available
            }
        }

        // Back and Close Buttons
        gui.setItem(40, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(41, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }


    private void toggleMaintenanceMode(Player player) {
        Archon plugin = Archon.getInstance();

        maintenanceMode = !maintenanceMode;
        if (maintenanceMode) {
            // Enable maintenance mode
            Bukkit.setWhitelist(true);

            if (plugin.getConfig().getBoolean("maintenance-mode.kick-players-on-enable", true)) {
                String kickMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("maintenance-mode.kick-message", "&cServer is under maintenance."));
                // Kick all non-exempt players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(plugin.getConfig().getString("maintenance-mode.bypass-permission", "archon.admin.bypassmaintenance"))) {
                        p.kickPlayer(kickMessage);
                    }
                }
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.maintenance-mode-enabled", "&aMaintenance mode has been enabled.")));
        } else {
            // Disable maintenance mode
            Bukkit.setWhitelist(false);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.maintenance-mode-disabled", "&aMaintenance mode has been disabled.")));
        }
    }


    private void stopServer(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Type the countdown time in seconds before the server stops (e.g., 30).");
        ChatInputHandler.expectingShutdownTime.put(player.getUniqueId(), true);
    }



    /**
     * Handles clicks in the Admin Settings GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onAdminSettingsGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.admin-settings-gui", "&bAdmin Settings"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "View Logs":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_VIEWLOGS)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view logs.");
                    return;
                }
                player.closeInventory();
                viewLogs(player);
                break;
            case "Manage Whitelist":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_WHITELIST)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to manage the whitelist.");
                    return;
                }
                player.closeInventory();
                openWhitelistManagementGUI(player);
                break;
            case "Toggle Whitelist":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_WHITELIST)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle the whitelist.");
                    return;
                }
                toggleWhitelist(player);
                break;
            case "Maintenance Mode":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_MAINTENANCE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle maintenance mode.");
                    return;
                }
                toggleMaintenanceMode(player);
                break;
            case "Stop Server":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_STOPSERVER)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to stop the server.");
                    return;
                }
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type the countdown time in seconds before the server stops (e.g., 30).");
                ChatInputHandler.expectingShutdownTime.put(player.getUniqueId(), true);
                break;
            case "Back":
                player.closeInventory();
                openMainGUI(player);
                break;
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }




    /**
     * Toggles the server's whitelist status.
     *
     * @param player The admin player.
     */
    private void toggleWhitelist(Player player) {
        boolean whitelistEnabled = Bukkit.hasWhitelist();
        Bukkit.setWhitelist(!whitelistEnabled);
        player.sendMessage(ChatColor.GREEN + "Whitelist has been " + (!whitelistEnabled ? "enabled" : "disabled") + ".");
    }
    /**
     * Opens the Whitelist Management GUI.
     *
     * @param player The admin player.
     */

    /**
     * Displays the server logs to the admin player.
     *
     * @param player The admin player.
     */
    private void viewLogs(Player player) {
        player.sendMessage(ChatColor.GREEN + "Fetching server logs...");
        player.sendMessage(ChatColor.YELLOW + "Type the page number to view logs or 'exit' to close.");
        ChatInputHandler.expectingLogPage.put(player.getUniqueId(), true);

        // Automatically show the first page
        Bukkit.getScheduler().runTaskAsynchronously(Archon.getInstance(), () -> {
            ChatInputHandler chatHandler = new ChatInputHandler();
            chatHandler.viewLogs(player, "1");
        });
    }

    private void openWhitelistManagementGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Whitelist Management");

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, borderItem);
        }

        // Close Button
        gui.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        int slot = 0;
        for (OfflinePlayer offlinePlayer : Bukkit.getWhitelistedPlayers()) {
            if (slot >= 45) break;

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                meta.setDisplayName(ChatColor.GREEN + offlinePlayer.getName());
                meta.setLore(Arrays.asList(ChatColor.YELLOW + "Click to remove from whitelist"));
                skull.setItemMeta(meta);

                gui.setItem(slot, skull);
                slot++;
            }
        }

        // Add Player Button
        gui.setItem(53, createGuiItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Add Player", "Add a player to the whitelist"));

        player.openInventory(gui);
    }

    /**
     * Handles clicks in the Whitelist Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onWhitelistManagementGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("gui.titles.whitelist-management-gui", "&dWhitelist Management"))
        );

        if (!ChatColor.stripColor(event.getView().getTitle()).equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("Close")) {
            player.closeInventory();
            return;
        }

        if (displayName.equals("Add Player")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Type the name of the player to add to the whitelist in chat.");
            ChatInputHandler.expectingWhitelistPlayer.put(player.getUniqueId(), "add");
            return;
        }

        // Remove player from whitelist
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(displayName);
        if (offlinePlayer != null) {
            offlinePlayer.setWhitelisted(false);
            player.sendMessage(ChatColor.GREEN + "Player " + displayName + " has been removed from the whitelist.");
            // Refresh GUI
            openWhitelistManagementGUI(player);
        }
    }



    // Additional methods for new features such as vanish, speed, etc.

    // Vanish Functionality
    private void toggleVanish(Player player) {
        boolean isVanished = vanishList.getOrDefault(player.getUniqueId(), false);
        vanishList.put(player.getUniqueId(), !isVanished);

        if (isVanished) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(Archon.getInstance(), player);
            }
            player.sendMessage(ChatColor.GREEN + "You are now visible to other players.");
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(Archon.getInstance(), player);
            }
            player.sendMessage(ChatColor.GREEN + "You are now vanished.");
        }
    }

    // Speed Control
    private void setSpeed(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Type the speed value (0.1 to 10) in chat.");
        ChatInputHandler.expectingSpeedValue.put(player.getUniqueId(), true);
    }

    /**
     * Opens the Player Management GUI.
     *
     * @param player The admin player.
     */
    private void openPlayerManagementGUI(Player player) {
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.player-management-gui", "&bPlayer Management"));
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Get player's preferred glass color or default from config
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative Border
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        gui.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(45, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(53, createGuiItem(Material.SEA_LANTERN, " "));

        // Title in the center top
        gui.setItem(4, createGuiItem(Material.PLAYER_HEAD, ChatColor.AQUA + "" + ChatColor.BOLD + "Player Management"));

        // Center area (slots 20-24,29-33)
        int[] itemSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        int index = 0;

        // Feature toggles
        boolean kickEnabled = plugin.getConfig().getBoolean("features.player-management.kick", true);
        boolean banEnabled = plugin.getConfig().getBoolean("features.player-management.ban", true);
        boolean muteEnabled = plugin.getConfig().getBoolean("features.player-management.mute", true);
        boolean freezeEnabled = plugin.getConfig().getBoolean("features.player-management.freeze", true);
        boolean teleportToEnabled = plugin.getConfig().getBoolean("features.player-management.teleport-to", true);
        boolean bringEnabled = plugin.getConfig().getBoolean("features.player-management.bring", true);
        boolean inspectEnabled = plugin.getConfig().getBoolean("features.player-management.inspect-inventory", true);
        boolean healEnabled = plugin.getConfig().getBoolean("features.player-management.heal", true);
        boolean feedEnabled = plugin.getConfig().getBoolean("features.player-management.feed", true);
        boolean setHealthEnabled = plugin.getConfig().getBoolean("features.player-management.set-health", true);
        boolean messageEnabled = plugin.getConfig().getBoolean("features.player-management.message", true);
        boolean permissionsEnabled = plugin.getConfig().getBoolean("features.player-management.manage-permissions", true);

        // Player Management Items
        List<ItemStack> items = new ArrayList<>();

        if (kickEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_KICK)) {
            items.add(createGuiItem(Material.BARRIER, "&cKick Player", "&7Kick a player from the server"));
        }
        if (banEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_BAN)) {
            items.add(createGuiItem(Material.GRAVEL, "&4Ban Player", "&7Ban a player from the server"));
        }
        if (muteEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_MUTE)) {
            items.add(createGuiItem(Material.NOTE_BLOCK, "&6Mute Player", "&7Mute a player in chat"));
        }
        if (freezeEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_FREEZE)) {
            items.add(createGuiItem(Material.BLUE_ICE, "&bFreeze Player", "&7Freeze a player"));
        }
        if (teleportToEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_TELEPORTTO)) {
            items.add(createGuiItem(Material.ENDER_PEARL, "&dTeleport to Player", "&7Teleport to a player"));
        }
        if (bringEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_BRING)) {
            items.add(createGuiItem(Material.FISHING_ROD, "&eBring Player", "&7Teleport a player to you"));
        }
        if (inspectEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_INSPECT)) {
            items.add(createGuiItem(Material.BOOK, "&aInspect Inventory", "&7View a player's inventory"));
        }
        if (healEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_HEAL)) {
            items.add(createGuiItem(Material.GOLDEN_APPLE, "&cHeal Player", "&7Restore a player's health"));
        }
        if (feedEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_FEED)) {
            items.add(createGuiItem(Material.COOKED_BEEF, "&6Feed Player", "&7Restore a player's hunger"));
        }
        if (setHealthEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_SETHEALTH)) {
            items.add(createGuiItem(Material.APPLE, "&4Set Health", "&7Set a player's health value"));
        }
        if (messageEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_MESSAGE)) {
            items.add(createGuiItem(Material.PAPER, "&bMessage Player", "&7Send a private message to a player"));
        }
        if (permissionsEnabled && player.hasPermission(PERM_PLAYERMANAGEMENT_PERMISSION)) {
            items.add(createGuiItem(Material.WRITABLE_BOOK, "&dManage Permissions", "&7Modify a player's permissions"));
        }

        // Place items in GUI
        for (ItemStack item : items) {
            if (index < itemSlots.length) {
                int slot = itemSlots[index];
                gui.setItem(slot, item);
                index++;
            } else {
                break; // No more slots available
            }
        }

        // Back and Close Buttons
        gui.setItem(48, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }



    /**
     * Opens the Server Management GUI.
     *
     * @param player The admin player.
     */
    private void openServerManagementGUI(Player player) {
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.server-management-gui", "&bServer Management"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Get player's preferred glass color or default from config
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative Border
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        gui.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(36, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(44, createGuiItem(Material.SEA_LANTERN, " "));

        // Title in the center top
        gui.setItem(4, createGuiItem(Material.COMMAND_BLOCK, ChatColor.AQUA + "" + ChatColor.BOLD + "Server Management"));

        // Center area (slots 20-24,29-33)
        int[] itemSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        int index = 0;

        // Feature toggles
        boolean changeTimeEnabled = plugin.getConfig().getBoolean("features.server-management.change-time", true);
        boolean changeWeatherEnabled = plugin.getConfig().getBoolean("features.server-management.change-weather", true);
        boolean manageWorldsEnabled = plugin.getConfig().getBoolean("features.server-management.manage-worlds", true);
        boolean serverStatsEnabled = plugin.getConfig().getBoolean("features.server-management.server-stats", true);
        boolean executeCommandEnabled = plugin.getConfig().getBoolean("features.server-management.execute-command", true);
        boolean managePluginsEnabled = plugin.getConfig().getBoolean("features.server-management.manage-plugins", true);

        // Server Management Items
        List<ItemStack> items = new ArrayList<>();

        if (changeTimeEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_CHANGETIME)) {
            items.add(createGuiItem(Material.CLOCK, "&eChange Time", "&7Set the time of day"));
        }
        if (changeWeatherEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_CHANGEWEATHER)) {
            items.add(createGuiItem(Material.COMPASS, "&9Change Weather", "&7Set the weather"));
        }
        if (manageWorldsEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEWORLDS)) {
            items.add(createGuiItem(Material.GRASS_BLOCK, "&aManage Worlds", "&7Create or delete worlds"));
        }
        if (serverStatsEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_SERVERSTATS)) {
            items.add(createGuiItem(Material.PAPER, "&5Server Stats", "&7View server statistics"));
        }
        if (executeCommandEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_EXECUTECOMMAND)) {
            items.add(createGuiItem(Material.COMMAND_BLOCK, "&cExecute Command", "&7Run a server command"));
        }
        if (managePluginsEnabled && player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEPLUGINS)) {
            items.add(createGuiItem(Material.REPEATER, "&dManage Plugins", "&7Enable or disable plugins"));
        }

        // Place items in GUI
        for (ItemStack item : items) {
            if (index < itemSlots.length) {
                int slot = itemSlots[index];
                gui.setItem(slot, item);
                index++;
            } else {
                break; // No more slots available
            }
        }

        // Back and Close Buttons
        gui.setItem(40, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(41, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }




    /**
     * Opens the Personal Tools GUI.
     *
     * @param player The admin player.
     */
    private void openPersonalToolsGUI(Player player) {
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.personal-tools-gui", "&bPersonal Tools"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        // Get player's preferred glass color or default from config
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative Border
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        gui.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(36, createGuiItem(Material.SEA_LANTERN, " "));
        gui.setItem(44, createGuiItem(Material.SEA_LANTERN, " "));

        // Title in the center top
        gui.setItem(4, createGuiItem(Material.NETHER_STAR, ChatColor.AQUA + "" + ChatColor.BOLD + "Personal Tools"));

        // Center area (slots 20-24,29-33)
        int[] itemSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        int index = 0;

        // Feature toggles
        boolean toggleFlyEnabled = plugin.getConfig().getBoolean("features.personal-tools.toggle-fly", true);
        boolean godModeEnabled = plugin.getConfig().getBoolean("features.personal-tools.god-mode", true);
        boolean healEnabled = plugin.getConfig().getBoolean("features.personal-tools.heal", true);
        boolean giveItemEnabled = plugin.getConfig().getBoolean("features.personal-tools.give-item", true);
        boolean enderChestEnabled = plugin.getConfig().getBoolean("features.personal-tools.ender-chest", true);
        boolean toggleGamemodeEnabled = plugin.getConfig().getBoolean("features.personal-tools.toggle-gamemode", true);
        boolean vanishEnabled = plugin.getConfig().getBoolean("features.personal-tools.vanish", true);
        boolean setSpeedEnabled = plugin.getConfig().getBoolean("features.personal-tools.set-speed", true);

        // Personal Tools Items
        List<ItemStack> items = new ArrayList<>();

        if (toggleFlyEnabled && player.hasPermission(PERM_PERSONALTOOLS_TOGGLEFLY)) {
            items.add(createGuiItem(Material.FEATHER, "&fToggle Fly", "&7Enable or disable fly mode"));
        }
        if (godModeEnabled && player.hasPermission(PERM_PERSONALTOOLS_GODMODE)) {
            items.add(createGuiItem(Material.NETHER_STAR, "&6God Mode", "&7Enable or disable god mode"));
        }
        if (healEnabled && player.hasPermission(PERM_PERSONALTOOLS_HEAL)) {
            items.add(createGuiItem(Material.GOLDEN_APPLE, "&cHeal", "&7Restore health and hunger"));
        }
        if (giveItemEnabled && player.hasPermission(PERM_PERSONALTOOLS_GIVEITEM)) {
            items.add(createGuiItem(Material.DIAMOND, "&eGive Item", "&7Give yourself an item"));
        }
        if (enderChestEnabled && player.hasPermission(PERM_PERSONALTOOLS_ENDERCHEST)) {
            items.add(createGuiItem(Material.ENDER_CHEST, "&1Ender Chest", "&7Open your ender chest"));
        }
        if (toggleGamemodeEnabled && player.hasPermission(PERM_PERSONALTOOLS_TOGGLEGAMEMODE)) {
            items.add(createGuiItem(Material.DIAMOND_SWORD, "&dToggle Game Mode", "&7Switch between creative and survival modes"));
        }
        if (vanishEnabled && player.hasPermission(PERM_PERSONALTOOLS_VANISH)) {
            items.add(createGuiItem(Material.GLASS, "&7Toggle Vanish", "&7Become invisible to other players"));
        }
        if (setSpeedEnabled && player.hasPermission(PERM_PERSONALTOOLS_SPEED)) {
            items.add(createGuiItem(Material.SUGAR, "&bSet Speed", "&7Set your movement speed"));
        }

        // Place items in GUI
        for (ItemStack item : items) {
            if (index < itemSlots.length) {
                int slot = itemSlots[index];
                gui.setItem(slot, item);
                index++;
            } else {
                break; // No more slots available
            }
        }

        // Back and Close Buttons
        gui.setItem(40, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(41, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }



    private void handlePlayerManagementAction(Player player, String action) {
        // Check if the player has the required permission
        String permission = getPermissionForAction(action);
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to " + action.toLowerCase() + ".");
            return;
        }
        player.closeInventory();
        openPlayerSelector(player, action);
    }

    /**
     * Gets the permission node required for a given action.
     *
     * @param action The action name.
     * @return The permission node, or null if not found.
     */
    private String getPermissionForAction(String action) {
        switch (action) {
            case "Kick Player":
                return PERM_PLAYERMANAGEMENT_KICK;
            case "Ban Player":
                return PERM_PLAYERMANAGEMENT_BAN;
            case "Mute Player":
                return PERM_PLAYERMANAGEMENT_MUTE;
            case "Freeze Player":
                return PERM_PLAYERMANAGEMENT_FREEZE;
            case "Teleport to Player":
                return PERM_PLAYERMANAGEMENT_TELEPORTTO;
            case "Bring Player":
                return PERM_PLAYERMANAGEMENT_BRING;
            case "Inspect Inventory":
                return PERM_PLAYERMANAGEMENT_INSPECT;
            case "Heal Player":
                return PERM_PLAYERMANAGEMENT_HEAL;
            case "Feed Player":
                return PERM_PLAYERMANAGEMENT_FEED;
            case "Set Health":
                return PERM_PLAYERMANAGEMENT_SETHEALTH;
            case "Message Player":
                return PERM_PLAYERMANAGEMENT_MESSAGE;
            case "Manage Permissions":
                return PERM_PLAYERMANAGEMENT_PERMISSION;
            default:
                return null;
        }
    }

    /**
     * Handles clicks in the Player Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    /**
     * Handles clicks in the Player Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPlayerManagementGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.player-management-gui", "&bPlayer Management"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        Material clickedType = clickedItem.getType();
        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());



        switch (itemName) {
            case "Kick Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_KICK)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to kick players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Ban Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_BAN)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to ban players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Mute Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_MUTE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to mute players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Freeze Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_FREEZE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to freeze players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Teleport to Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_TELEPORTTO)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to teleport to players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Bring Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_BRING)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to bring players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Inspect Inventory":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_INSPECT)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to inspect inventories.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Heal Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_HEAL)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to heal players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Feed Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_FEED)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to feed players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Set Health":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_SETHEALTH)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set players' health.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Message Player":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_MESSAGE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to message players.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Manage Permissions":
                if (!player.hasPermission(PERM_PLAYERMANAGEMENT_PERMISSION)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to manage permissions.");
                    return;
                }
                player.closeInventory();
                openPlayerSelector(player, itemName);
                break;
            case "Back":
                player.closeInventory();
                openMainGUI(player);
                break;
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }


    /**
     * Handles clicks in the Server Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onServerManagementGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.server-management-gui", "&bServer Management"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        Material clickedType = clickedItem.getType();
        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Change Time":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_CHANGETIME)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to change time.");
                    return;
                }
                player.closeInventory();
                openTimeGUI(player);
                break;
            case "Change Weather":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_CHANGEWEATHER)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to change weather.");
                    return;
                }
                player.closeInventory();
                openWeatherGUI(player);
                break;
            case "Manage Worlds":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEWORLDS)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to manage worlds.");
                    return;
                }
                player.closeInventory();
                openWorldManagementGUI(player);
                break;
            case "Server Stats":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_SERVERSTATS)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view server stats.");
                    return;
                }
                player.closeInventory();
                displayServerStats(player);
                break;
            case "Execute Command":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_EXECUTECOMMAND)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to execute commands.");
                    return;
                }
                player.closeInventory();
                ChatInputHandler.expectingCommand.put(player.getUniqueId(), true);
                player.sendMessage(ChatColor.YELLOW + "Please type the command to execute in chat.");
                break;
            case "Manage Plugins":
                if (!player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEPLUGINS)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to manage plugins.");
                    return;
                }
                player.closeInventory();
                openPluginManagementGUI(player);
                break;
            case "Back":
                player.closeInventory();
                openMainGUI(player);
                break;
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }


    /**
     * Handles clicks in the Personal Tools GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPersonalToolsGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.personal-tools-gui", "&bPersonal Tools"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Toggle Fly":
                if (!player.hasPermission(PERM_PERSONALTOOLS_TOGGLEFLY)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle fly mode.");
                    return;
                }
                toggleFly(player);
                break;
            case "God Mode":
                if (!player.hasPermission(PERM_PERSONALTOOLS_GODMODE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle god mode.");
                    return;
                }
                toggleGodMode(player);
                break;
            case "Heal":
                if (!player.hasPermission(PERM_PERSONALTOOLS_HEAL)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to heal.");
                    return;
                }
                healPlayer(player);
                break;
            case "Give Item":
                if (!player.hasPermission(PERM_PERSONALTOOLS_GIVEITEM)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to give items.");
                    return;
                }
                player.closeInventory();
                openItemGiveGUI(player, 0);
                break;
            case "Ender Chest":
                if (!player.hasPermission(PERM_PERSONALTOOLS_ENDERCHEST)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to open ender chests.");
                    return;
                }
                player.closeInventory();
                player.openInventory(player.getEnderChest());
                break;
            case "Toggle Game Mode":
                if (!player.hasPermission(PERM_PERSONALTOOLS_TOGGLEGAMEMODE)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle game mode.");
                    return;
                }
                toggleGameMode(player);
                break;
            case "Toggle Vanish":
                if (!player.hasPermission(PERM_PERSONALTOOLS_VANISH)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to toggle vanish mode.");
                    return;
                }
                toggleVanish(player);
                break;
            case "Set Speed":
                if (!player.hasPermission(PERM_PERSONALTOOLS_SPEED)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set speed.");
                    return;
                }
                setSpeed(player);
                break;
            case "Back":
                player.closeInventory();
                openMainGUI(player);
                break;
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }


    /**
     * Opens the player selector GUI for a specific action.
     *
     * @param player The admin player.
     * @param action The action to perform.
     */
    private void openPlayerSelector(Player player, String action) {
        Archon plugin = Archon.getInstance();
        // Get base GUI title from config
        String baseTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.player-selector-gui", "&aSelect a Player - %action%"));
        // Replace %action% with the actual action
        String title = baseTitle.replace("%action%", action);

        Inventory playerSelector = Bukkit.createInventory(null, 54, title);

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            playerSelector.setItem(i, borderItem);
        }

        // Close Button
        playerSelector.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break; // Prevent overflow

            // Create a player head with the target's skin
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(ChatColor.GREEN + target.getName());
                meta.setLore(Arrays.asList(ChatColor.YELLOW + "Click to " + action.toLowerCase() + " " + target.getName()));
                head.setItemMeta(meta);

                playerSelector.setItem(slot, head);
                slot++;
            }
        }

        player.openInventory(playerSelector);
    }

    /**
     * Opens the time control GUI.
     *
     * @param player The admin player.
     */
    private void openTimeGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.time-gui", "&eTime Control"));
        Inventory timeGUI = Bukkit.createInventory(null, 9, title);

        timeGUI.setItem(2, createGuiItem(Material.SUNFLOWER, ChatColor.YELLOW + "Day", "Set time to day"));
        timeGUI.setItem(4, createGuiItem(Material.CLOCK, ChatColor.GOLD + "Noon", "Set time to noon"));
        timeGUI.setItem(6, createGuiItem(Material.BLACK_BED, ChatColor.BLUE + "Night", "Set time to night"));

        player.openInventory(timeGUI);
    }

    /**
     * Opens the weather control GUI.
     *
     * @param player The admin player.
     */
    private void openWeatherGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.weather-gui", "&eWeather Control"));
        Inventory weatherGUI = Bukkit.createInventory(null, 9, title);

        weatherGUI.setItem(2, createGuiItem(Material.WATER_BUCKET, ChatColor.AQUA + "Rain", "Set weather to rain"));
        weatherGUI.setItem(4, createGuiItem(Material.TRIDENT, ChatColor.GRAY + "Thunder", "Set weather to thunder"));
        weatherGUI.setItem(6, createGuiItem(Material.SUNFLOWER, ChatColor.YELLOW + "Clear", "Set weather to clear"));

        player.openInventory(weatherGUI);
    }

    /**
     * Opens the world management GUI.
     *
     * @param player The admin player.
     */
    private void openWorldManagementGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.world-management-gui", "&aWorld Management"));
        Inventory worldGUI = Bukkit.createInventory(null, 9, title);

        worldGUI.setItem(2, createGuiItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Create World", "Create a new world"));
        worldGUI.setItem(4, createGuiItem(Material.BARRIER, ChatColor.RED + "Delete World", "Delete an existing world"));
        worldGUI.setItem(6, createGuiItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Teleport to World", "Teleport to a world"));

        player.openInventory(worldGUI);
    }

    /**
     * Opens the plugin management GUI.
     *
     * @param player The admin player.
     */
    private void openPluginManagementGUI(Player player) {
        Archon plugin = Archon.getInstance();
        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.plugin-management-gui", "&bManage Plugins"));
        Inventory pluginGUI = Bukkit.createInventory(null, 54, title);

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        // Decorative Border
        ItemStack borderItem = createGuiItem(glassMaterial, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                pluginGUI.setItem(i, borderItem);
            }
        }

        // Decorative Corners
        pluginGUI.setItem(0, createGuiItem(Material.SEA_LANTERN, " "));
        pluginGUI.setItem(8, createGuiItem(Material.SEA_LANTERN, " "));
        pluginGUI.setItem(45, createGuiItem(Material.SEA_LANTERN, " "));
        pluginGUI.setItem(53, createGuiItem(Material.SEA_LANTERN, " "));

        // Title in the center top
        pluginGUI.setItem(4, createGuiItem(Material.REPEATER, ChatColor.AQUA + "" + ChatColor.BOLD + "Manage Plugins"));

        int slot = 10;
        for (Plugin targetPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (slot >= 44) break; // Prevent overflow

            // Prevent the admin from disabling essential plugins
            boolean isEssential = targetPlugin.getName().equalsIgnoreCase("Archon"); // Replace "Archon" with your plugin's name

            Material icon = targetPlugin.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
            String actionText = targetPlugin.isEnabled() ? (isEssential ? "Cannot disable" : "Click to disable") : "Restart server to enable";

            ItemStack pluginItem = createGuiItem(icon, ChatColor.WHITE + targetPlugin.getName(),
                    ChatColor.GRAY + "Version: " + targetPlugin.getDescription().getVersion(),
                    ChatColor.YELLOW + actionText);

            pluginGUI.setItem(slot, pluginItem);
            slot++;

            // Skip border slots
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
        }
        // Back and Close Buttons
        pluginGUI.setItem(48, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        pluginGUI.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(pluginGUI);
    }



    /**
     * Opens the item give GUI with pagination.
     *
     * @param player The admin player.
     * @param page   The page number to display.
     */
    private void openItemGiveGUI(Player player, int page) {
        Archon plugin = Archon.getInstance();
        // Get base GUI title from config
        String baseTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.item-give-gui", "&bItem Give"));
        String title = baseTitle + " - Page " + (page + 1);

        Inventory itemGUI = Bukkit.createInventory(null, 54, title);

        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isItem()) {
                materials.add(material);
            }
        }

        int itemsPerPage = 45;
        int maxPage = (materials.size() - 1) / itemsPerPage;

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, materials.size());

        for (int i = startIndex; i < endIndex; i++) {
            Material material = materials.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.WHITE + material.name());
                item.setItemMeta(meta);
            }

            itemGUI.setItem(i - startIndex, item);
        }

        // Navigation and Close Buttons
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            itemGUI.setItem(i, borderItem);
        }

        if (page > 0) {
            itemGUI.setItem(48, createGuiItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
        } else {
            itemGUI.setItem(48, borderItem);
        }

        if (page < maxPage) {
            itemGUI.setItem(50, createGuiItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
        } else {
            itemGUI.setItem(50, borderItem);
        }

        itemGUI.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        itemGUIPages.put(player.getUniqueId(), page);
        player.openInventory(itemGUI);
    }


    /**
     * Displays server statistics to the admin player.
     *
     * @param player The admin player.
     */
    private void displayServerStats(Player player) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        double tps = getServerTPS();
        long availableMemory = getAvailableMemory();

        player.sendMessage(ChatColor.GREEN + "Server Statistics:");
        player.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + onlinePlayers + "/" + maxPlayers);
        player.sendMessage(ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + String.format("%.2f", tps));
        player.sendMessage(ChatColor.YELLOW + "Available Memory: " + ChatColor.WHITE + availableMemory + " MB");
    }

    /**
     * Gets the current server TPS (Ticks Per Second).
     *
     * @return The server TPS.
     */
    private double getServerTPS() {
        try {
            double[] tps = Bukkit.getServer().getTPS();
            return tps[0];
        } catch (NoSuchMethodError e) {
            return 20.0;
        }
    }

    /**
     * Gets the available memory of the server in MB.
     *
     * @return The available memory in MB.
     */
    private long getAvailableMemory() {
        long freeMemory = Runtime.getRuntime().freeMemory() / 1048576;
        return freeMemory;
    }

    /**
     * Toggles fly mode for the admin player.
     *
     * @param player The admin player.
     */
    private void toggleFly(Player player) {
        boolean canFly = player.getAllowFlight();
        player.setAllowFlight(!canFly);
        player.sendMessage(ChatColor.GREEN + "Fly mode " + (canFly ? "disabled" : "enabled") + ".");
    }

    /**
     * Toggles god mode for the admin player.
     *
     * @param player The admin player.
     */
    private void toggleGodMode(Player player) {
        boolean isGodMode = godModeList.getOrDefault(player.getUniqueId(), false);
        godModeList.put(player.getUniqueId(), !isGodMode);
        player.sendMessage(ChatColor.GREEN + "God mode " + (!isGodMode ? "enabled" : "disabled") + ".");
    }

    /**
     * Event handler for player damage to implement god mode.
     *
     * @param event The EntityDamageEvent.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godModeList.getOrDefault(player.getUniqueId(), false)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Toggles game mode between creative and survival for the admin player.
     *
     * @param player The admin player.
     */
    private void toggleGameMode(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage(ChatColor.GREEN + "Game mode set to Survival.");
        } else {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(ChatColor.GREEN + "Game mode set to Creative.");
        }
    }

    /**
     * Heals the admin player to full health and hunger.
     *
     * @param player The admin player.
     */
    private void healPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.sendMessage(ChatColor.GREEN + "You have been healed.");
    }

    // The rest of the event handlers and methods remain the same.

    /**
     * Handles clicks in the plugin management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPluginManagementGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon pluginInstance = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                pluginInstance.getConfig().getString("gui.titles.plugin-management-gui", "&bManage Plugins"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        // Get player's preferred glass color or default
        String defaultGlassColorName = pluginInstance.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(player.getUniqueId(), defaultGlassMaterial);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        Material clickedType = clickedItem.getType();
        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        if (displayName.equals("Back")) {
            player.closeInventory();
            openServerManagementGUI(player);
            return;
        } else if (displayName.equals("Close")) {
            player.closeInventory();
            return;
        }

        String pluginName = displayName;
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (targetPlugin == null) {
            player.sendMessage(ChatColor.RED + "Plugin not found.");
            return;
        }

        if (targetPlugin.equals(Archon.getInstance())) {
            player.sendMessage(ChatColor.RED + "You cannot disable this plugin through the GUI.");
            return;
        }

        try {
            if (targetPlugin.isEnabled()) {
                // Disable the plugin
                Bukkit.getPluginManager().disablePlugin(targetPlugin);
                player.sendMessage(ChatColor.GREEN + "Plugin " + pluginName + " has been disabled.");
            } else {
                // Inform the player that re-enabling plugins requires a server restart
                player.sendMessage(ChatColor.RED + "Enabling plugins at runtime is not supported. Please restart the server to enable this plugin.");
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while toggling the plugin.");
            e.printStackTrace();
        }

        // Refresh the GUI
        openPluginManagementGUI(player);
    }


    /**
     * Handles clicks in the player selector GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPlayerSelectorClick(InventoryClickEvent event) {
        Player admin = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        String rawTitle = ChatColor.stripColor(event.getView().getTitle());

        // Get base GUI title from config
        String baseTitle = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("gui.titles.player-selector-gui", "&aSelect a Player - %action%")
                ).split("-")[0].trim()
        );

        if (!rawTitle.startsWith(baseTitle)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("Close")) {
            admin.closeInventory();
            return;
        }

        Material clickedType = clickedItem.getType();

        // Get player's preferred glass color or default
        String defaultGlassColorName = plugin.getConfig().getString("gui.default-glass-color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        Material defaultGlassMaterial = Material.matchMaterial(defaultGlassColorName);
        if (defaultGlassMaterial == null || !defaultGlassMaterial.isItem()) {
            defaultGlassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        Material glassMaterial = playerGlassColor.getOrDefault(admin.getUniqueId(), defaultGlassMaterial);

        if (clickedType == glassMaterial || clickedType == Material.SEA_LANTERN) {
            return;
        }

        if (clickedType != Material.PLAYER_HEAD)
            return;

        String targetName = displayName;
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            admin.sendMessage(ChatColor.RED + "Player not found.");
            admin.closeInventory();
            return;
        }

        String action = rawTitle.substring(rawTitle.lastIndexOf('-') + 1).trim(); // Extract action from title

        // Handle the action
        switch (action) {
            case "Kick Player":
                target.kickPlayer(ChatColor.RED + "You have been kicked by an administrator.");
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been kicked.");
                break;
            case "Ban Player":
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), "You have been banned by an administrator.", null, admin.getName());
                target.kickPlayer(ChatColor.RED + "You have been banned by an administrator.");
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been banned.");
                break;
            case "Mute Player":
                toggleMutePlayer(target, admin);
                break;
            case "Freeze Player":
                toggleFreezePlayer(target, admin);
                break;
            case "Teleport to Player":
                admin.teleport(target.getLocation());
                admin.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + ".");
                break;
            case "Bring Player":
                target.teleport(admin.getLocation());
                admin.sendMessage(ChatColor.GREEN + "Brought " + target.getName() + " to your location.");
                break;
            case "Inspect Inventory":
                admin.openInventory(target.getInventory());
                admin.sendMessage(ChatColor.GREEN + "Inspecting " + target.getName() + "'s inventory.");
                break;
            case "Heal Player":
                target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                target.sendMessage(ChatColor.GREEN + "You have been healed by an administrator.");
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been healed.");
                break;
            case "Feed Player":
                target.setFoodLevel(20);
                target.setSaturation(20);
                target.sendMessage(ChatColor.GREEN + "Your hunger has been restored by an administrator.");
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been fed.");
                break;
            case "Set Health":
                admin.closeInventory();
                admin.sendMessage(ChatColor.YELLOW + "Type the health value (1 - " + target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + ") for " + target.getName() + " in chat.");
                ChatInputHandler.expectingHealthValue.put(admin.getUniqueId(), target.getUniqueId());
                break;
            case "Message Player":
                admin.closeInventory();
                admin.sendMessage(ChatColor.YELLOW + "Type the message to send to " + target.getName() + " in chat.");
                ChatInputHandler.expectingPrivateMessage.put(admin.getUniqueId(), target.getUniqueId());
                break;
            case "Manage Permissions":
                admin.closeInventory();
                openPermissionManagementGUI(admin, target);
                break;
            default:
                admin.sendMessage(ChatColor.RED + "Invalid action.");
                break;
        }
    }


    /**
     * Opens the Permission Management GUI for a player.
     *
     * @param admin  The admin player.
     * @param target The target player.
     */
    private void openPermissionManagementGUI(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, PERMISSION_GUI_TITLE + " - " + target.getName());

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 18; i < 27; i++) {
            gui.setItem(i, borderItem);
        }

        // Permission Management Items
        gui.setItem(10, createGuiItem(Material.GREEN_WOOL, ChatColor.GREEN + "Add Permission", "Add a permission to " + target.getName()));
        gui.setItem(12, createGuiItem(Material.RED_WOOL, ChatColor.RED + "Remove Permission", "Remove a permission from " + target.getName()));

        // Back and Close Buttons
        gui.setItem(22, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(23, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        // Store the target player's UUID for reference
        ChatInputHandler.permissionTargets.put(admin.getUniqueId(), target.getUniqueId());

        admin.openInventory(gui);
    }

    /**
     * Handles clicks in the Permission Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPermissionManagementGUIClick(InventoryClickEvent event) {
        Player admin = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        String rawTitle = ChatColor.stripColor(event.getView().getTitle());

        // Get base GUI title from config
        String baseTitle = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("gui.titles.permission-management-gui", "&5Permission Management"))
        );

        if (!rawTitle.startsWith(baseTitle))
            return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        UUID targetUUID = ChatInputHandler.permissionTargets.get(admin.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null) {
            admin.sendMessage(ChatColor.RED + "Target player not found.");
            admin.closeInventory();
            return;
        }

        switch (itemName) {
            case "Add Permission":
                admin.closeInventory();
                admin.sendMessage(ChatColor.YELLOW + "Type the permission node to add to " + target.getName() + " in chat.");
                ChatInputHandler.expectingPermissionNode.put(admin.getUniqueId(), new AbstractMap.SimpleEntry<>(target.getUniqueId(), true));
                break;
            case "Remove Permission":
                admin.closeInventory();
                admin.sendMessage(ChatColor.YELLOW + "Type the permission node to remove from " + target.getName() + " in chat.");
                ChatInputHandler.expectingPermissionNode.put(admin.getUniqueId(), new AbstractMap.SimpleEntry<>(target.getUniqueId(), false));
                break;
            case "Back":
                admin.closeInventory();
                openPlayerManagementGUI(admin);
                break;
            case "Close":
                admin.closeInventory();
                break;
            default:
                break;
        }
    }


    /**
     * Toggles mute status for a player.
     *
     * @param target The target player.
     * @param admin  The admin player.
     */
    private void toggleMutePlayer(Player admin, Player target) {
        boolean isMuted = muteList.getOrDefault(target.getUniqueId(), false);
        if (isMuted) {
            muteList.remove(target.getUniqueId());
            target.sendMessage(ChatColor.GREEN + "You have been unmuted by an administrator.");
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been unmuted.");
        } else {
            muteList.put(target.getUniqueId(), true);
            target.sendMessage(ChatColor.RED + "You have been muted by an administrator.");
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been muted.");
        }
    }


    /**
     * Event handler for player chat to implement mute functionality.
     *
     * @param event The AsyncPlayerChatEvent.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (muteList.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are muted and cannot chat.");
        }
    }

    /**
     * Toggles freeze status for a player.
     *
     * @param target The target player.
     * @param admin  The admin player.
     */
    private void toggleFreezePlayer(Player target, Player admin) {
        boolean isFrozen = freezeList.getOrDefault(target.getUniqueId(), false);
        freezeList.put(target.getUniqueId(), !isFrozen);

        if (isFrozen) {
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been unfrozen.");
            target.sendMessage(ChatColor.YELLOW + "You have been unfrozen.");
        } else {
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been frozen.");
            target.sendMessage(ChatColor.RED + "You have been frozen.");
        }
    }

    /**
     * Event handler for player movement to implement freeze functionality.
     *
     * @param event The PlayerMoveEvent.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (freezeList.getOrDefault(player.getUniqueId(), false)) {
            if (event.getFrom().distanceSquared(event.getTo()) > 0) {
                event.setTo(event.getFrom());
                player.sendMessage(ChatColor.RED + "You are frozen and cannot move.");
            }
        }
    }

    /**
     * Handles clicks in the time control GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onTimeGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.time-gui", "&eTime Control"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Day":
                player.getWorld().setTime(1000);
                player.sendMessage(ChatColor.GREEN + "Time set to day.");
                break;
            case "Noon":
                player.getWorld().setTime(6000);
                player.sendMessage(ChatColor.GREEN + "Time set to noon.");
                break;
            case "Night":
                player.getWorld().setTime(13000);
                player.sendMessage(ChatColor.GREEN + "Time set to night.");
                break;
            default:
                break;
        }

        player.closeInventory();
    }


    /**
     * Handles clicks in the weather control GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onWeatherGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.weather-gui", "&eWeather Control"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Clear":
                player.getWorld().setStorm(false);
                player.getWorld().setThundering(false);
                player.sendMessage(ChatColor.GREEN + "Weather set to clear.");
                break;
            case "Rain":
                player.getWorld().setStorm(true);
                player.getWorld().setThundering(false);
                player.sendMessage(ChatColor.GREEN + "Weather set to rain.");
                break;
            case "Thunder":
                player.getWorld().setStorm(true);
                player.getWorld().setThundering(true);
                player.sendMessage(ChatColor.GREEN + "Weather set to thunderstorm.");
                break;
            default:
                break;
        }

        player.closeInventory();
    }


    /**
     * Handles clicks in the world management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onWorldManagementGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get GUI title from config
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.world-management-gui", "&aWorld Management"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Create World":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type the name of the new world in chat.");
                ChatInputHandler.expectingWorldName.put(player.getUniqueId(), "create");
                break;
            case "Delete World":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type the name of the world to delete in chat.");
                ChatInputHandler.expectingWorldName.put(player.getUniqueId(), "delete");
                break;
            case "Teleport to World":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type the name of the world to teleport to in chat.");
                ChatInputHandler.expectingWorldName.put(player.getUniqueId(), "teleport");
                break;
            default:
                break;
        }
    }


    /**
     * Handles clicks in the item give GUI with pagination.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onItemGiveGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Archon plugin = Archon.getInstance();

        // Get base GUI title from config (without the page number)
        String baseTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.titles.item-give-gui", "&bItem Give"));

        if (!event.getView().getTitle().startsWith(baseTitle)) return;

        event.setCancelled(true);

        UUID playerUUID = player.getUniqueId();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        String displayName = clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())
                : "";

        if (displayName.equals("Next Page")) {
            int currentPage = itemGUIPages.getOrDefault(playerUUID, 0);
            openItemGiveGUI(player, currentPage + 1);
        } else if (displayName.equals("Previous Page")) {
            int currentPage = itemGUIPages.getOrDefault(playerUUID, 0);
            if (currentPage > 0) {
                openItemGiveGUI(player, currentPage - 1);
            }
        } else if (displayName.equals("Close")) {
            player.closeInventory();
            itemGUIPages.remove(playerUUID);
        } else {
            Material material = clickedItem.getType();
            if (material != Material.GRAY_STAINED_GLASS_PANE && material != Material.AIR) {
                player.getInventory().addItem(new ItemStack(material, 1));
                player.sendMessage(ChatColor.GREEN + "You have been given 1x " + material.name() + ".");
            }
        }
    }
}
