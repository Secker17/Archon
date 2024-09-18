package com.asfaltios.archon;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.OfflinePlayer;
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

import java.util.*;

public class AdminGUI implements Listener {

    // GUI Titles
    private static final String MAIN_GUI_TITLE = ChatColor.DARK_GREEN + "Archon Admin Panel";
    private static final String PLAYER_MANAGEMENT_GUI_TITLE = ChatColor.GREEN + "Player Management";
    private static final String SERVER_MANAGEMENT_GUI_TITLE = ChatColor.BLUE + "Server Management";
    private static final String PERSONAL_TOOLS_GUI_TITLE = ChatColor.GOLD + "Personal Tools";
    private static final String PLAYER_SELECTOR_TITLE = ChatColor.BLUE + "Select a Player - ";
    private static final String TIME_GUI_TITLE = ChatColor.GOLD + "Change Time";
    private static final String WEATHER_GUI_TITLE = ChatColor.BLUE + "Change Weather";
    private static final String WORLD_GUI_TITLE = ChatColor.GREEN + "Manage Worlds";
    private static final String ITEM_GIVE_GUI_TITLE = ChatColor.YELLOW + "Give Item";
    private static final String PLUGIN_GUI_TITLE = ChatColor.LIGHT_PURPLE + "Manage Plugins";

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

    private static final String PERM_SERVERMANAGEMENT_CHANGETIME = "archon.admin.servermanagement.changetime";
    private static final String PERM_SERVERMANAGEMENT_CHANGEWEATHER = "archon.admin.servermanagement.changeweather";
    private static final String PERM_SERVERMANAGEMENT_MANAGEWORLDS = "archon.admin.servermanagement.manageworlds";
    private static final String PERM_SERVERMANAGEMENT_SERVERSTATS = "archon.admin.servermanagement.serverstats";
    private static final String PERM_SERVERMANAGEMENT_EXECUTECOMMAND = "archon.admin.servermanagement.executecommand";
    private static final String PERM_SERVERMANAGEMENT_MANAGEPLUGINS = "archon.admin.servermanagement.manageplugins";

    private static final String PERM_PERSONALTOOLS_TOGGLEFLY = "archon.admin.personaltools.togglefly";
    private static final String PERM_PERSONALTOOLS_GODMODE = "archon.admin.personaltools.godmode";
    private static final String PERM_PERSONALTOOLS_HEAL = "archon.admin.personaltools.heal";
    private static final String PERM_PERSONALTOOLS_GIVEITEM = "archon.admin.personaltools.giveitem";
    private static final String PERM_PERSONALTOOLS_ENDERCHEST = "archon.admin.personaltools.enderchest";
    private static final String PERM_PERSONALTOOLS_TOGGLEGAMEMODE = "archon.admin.personaltools.togglegamemode";

    // Mute and Freeze Lists
    private static final Map<UUID, Boolean> muteList = new HashMap<>();
    private static final Map<UUID, Boolean> freezeList = new HashMap<>();
    private static final Map<UUID, Boolean> godModeList = new HashMap<>();

    // Pagination for Item GUI
    private static final Map<UUID, Integer> itemGUIPages = new HashMap<>();

    /**
     * Opens the main admin GUI for the player.
     *
     * @param player The player to open the GUI for.
     */
    public static void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, MAIN_GUI_TITLE);

        // Decorative Borders
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17) {
                gui.setItem(i, borderItem);
            }
        }

        // Section Buttons
        if (player.hasPermission(PERM_PLAYERMANAGEMENT)) {
            gui.setItem(10, createGuiItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Player Management", "Manage players"));
        }

        if (player.hasPermission(PERM_SERVERMANAGEMENT)) {
            gui.setItem(13, createGuiItem(Material.COMMAND_BLOCK, ChatColor.BLUE + "Server Management", "Manage server settings"));
        }

        if (player.hasPermission(PERM_PERSONALTOOLS)) {
            gui.setItem(16, createGuiItem(Material.DIAMOND_SWORD, ChatColor.GOLD + "Personal Tools", "Access personal admin tools"));
        }

        // Close Button
        gui.setItem(22, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
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
            meta.setDisplayName(name);
            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
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

        if (!event.getView().getTitle().equals(MAIN_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Prevent interaction with borders
        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
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
            case "Close":
                player.closeInventory();
                break;
            default:
                break;
        }
    }

    /**
     * Opens the Player Management GUI.
     *
     * @param player The admin player.
     */
    private void openPlayerManagementGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, PLAYER_MANAGEMENT_GUI_TITLE);

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, borderItem);
        }

        // Player Management Items
        int slot = 10;
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_KICK)) {
            gui.setItem(slot++, createGuiItem(Material.BARRIER, ChatColor.RED + "Kick Player", "Kick a player from the server"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_BAN)) {
            gui.setItem(slot++, createGuiItem(Material.ANVIL, ChatColor.DARK_RED + "Ban Player", "Ban a player from the server"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_MUTE)) {
            gui.setItem(slot++, createGuiItem(Material.JUKEBOX, ChatColor.GOLD + "Mute Player", "Mute a player in chat"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_FREEZE)) {
            gui.setItem(slot++, createGuiItem(Material.ICE, ChatColor.AQUA + "Freeze Player", "Freeze a player"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_TELEPORTTO)) {
            gui.setItem(slot++, createGuiItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Teleport to Player", "Teleport to a player"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_BRING)) {
            gui.setItem(slot++, createGuiItem(Material.FISHING_ROD, ChatColor.YELLOW + "Bring Player", "Teleport a player to you"));
        }
        if (player.hasPermission(PERM_PLAYERMANAGEMENT_INSPECT)) {
            gui.setItem(slot++, createGuiItem(Material.BOOK, ChatColor.GREEN + "Inspect Inventory", "View a player's inventory"));
        }

        // Back and Close Buttons
        gui.setItem(30, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(31, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }

    /**
     * Opens the Server Management GUI.
     *
     * @param player The admin player.
     */
    private void openServerManagementGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, SERVER_MANAGEMENT_GUI_TITLE);

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, borderItem);
        }

        // Server Management Items
        int slot = 10;
        if (player.hasPermission(PERM_SERVERMANAGEMENT_CHANGETIME)) {
            gui.setItem(slot++, createGuiItem(Material.CLOCK, ChatColor.YELLOW + "Change Time", "Set the time of day"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_CHANGEWEATHER)) {
            gui.setItem(slot++, createGuiItem(Material.COMPASS, ChatColor.BLUE + "Change Weather", "Set the weather"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEWORLDS)) {
            gui.setItem(slot++, createGuiItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Manage Worlds", "Create or delete worlds"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_SERVERSTATS)) {
            gui.setItem(slot++, createGuiItem(Material.PAPER, ChatColor.DARK_PURPLE + "Server Stats", "View server statistics"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_EXECUTECOMMAND)) {
            gui.setItem(slot++, createGuiItem(Material.COMMAND_BLOCK, ChatColor.RED + "Execute Command", "Run a server command"));
        }
        if (player.hasPermission(PERM_SERVERMANAGEMENT_MANAGEPLUGINS)) {
            gui.setItem(slot++, createGuiItem(Material.REPEATER, ChatColor.LIGHT_PURPLE + "Manage Plugins", "Enable or disable plugins"));
        }

        // Back and Close Buttons
        gui.setItem(30, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(31, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }

    /**
     * Opens the Personal Tools GUI.
     *
     * @param player The admin player.
     */
    private void openPersonalToolsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, PERSONAL_TOOLS_GUI_TITLE);

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, borderItem);
        }

        // Personal Tools Items
        int slot = 10;
        if (player.hasPermission(PERM_PERSONALTOOLS_TOGGLEFLY)) {
            gui.setItem(slot++, createGuiItem(Material.FEATHER, ChatColor.WHITE + "Toggle Fly", "Enable or disable fly mode"));
        }
        if (player.hasPermission(PERM_PERSONALTOOLS_GODMODE)) {
            gui.setItem(slot++, createGuiItem(Material.NETHER_STAR, ChatColor.GOLD + "God Mode", "Enable or disable god mode"));
        }
        if (player.hasPermission(PERM_PERSONALTOOLS_HEAL)) {
            gui.setItem(slot++, createGuiItem(Material.GOLDEN_APPLE, ChatColor.RED + "Heal", "Restore health and hunger"));
        }
        if (player.hasPermission(PERM_PERSONALTOOLS_GIVEITEM)) {
            gui.setItem(slot++, createGuiItem(Material.DIAMOND, ChatColor.YELLOW + "Give Item", "Give yourself an item"));
        }
        if (player.hasPermission(PERM_PERSONALTOOLS_ENDERCHEST)) {
            gui.setItem(slot++, createGuiItem(Material.ENDER_CHEST, ChatColor.DARK_BLUE + "Ender Chest", "Open your ender chest"));
        }
        if (player.hasPermission(PERM_PERSONALTOOLS_TOGGLEGAMEMODE)) {
            gui.setItem(slot++, createGuiItem(Material.DIAMOND_SWORD, ChatColor.LIGHT_PURPLE + "Toggle Game Mode", "Switch between creative and survival modes"));
        }

        // Back and Close Buttons
        gui.setItem(30, createGuiItem(Material.ARROW, ChatColor.GREEN + "Back"));
        gui.setItem(31, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        player.openInventory(gui);
    }

    /**
     * Handles clicks in the Player Management GUI.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onPlayerManagementGUIClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(PLAYER_MANAGEMENT_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
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

        if (!event.getView().getTitle().equals(SERVER_MANAGEMENT_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
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

        if (!event.getView().getTitle().equals(PERSONAL_TOOLS_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
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
        Inventory playerSelector = Bukkit.createInventory(null, 54, PLAYER_SELECTOR_TITLE + action);

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
        Inventory timeGUI = Bukkit.createInventory(null, 9, TIME_GUI_TITLE);

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
        Inventory weatherGUI = Bukkit.createInventory(null, 9, WEATHER_GUI_TITLE);

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
        Inventory worldGUI = Bukkit.createInventory(null, 9, WORLD_GUI_TITLE);

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
        Inventory pluginGUI = Bukkit.createInventory(null, 54, PLUGIN_GUI_TITLE);

        // Decorative Border
        ItemStack borderItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            pluginGUI.setItem(i, borderItem);
        }

        // Close Button
        pluginGUI.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Close"));

        int slot = 0;
        for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (slot >= 45) break; // Prevent overflow
            Material icon = plugin.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
            ItemStack pluginItem = createGuiItem(icon, ChatColor.WHITE + plugin.getName(),
                    ChatColor.GRAY + "Version: " + plugin.getDescription().getVersion(),
                    ChatColor.YELLOW + (plugin.isEnabled() ? "Click to disable" : "Click to enable"));
            pluginGUI.setItem(slot, pluginItem);
            slot++;
        }

        player.openInventory(pluginGUI);
    }

    /**
     * Opens the item give GUI with pagination.
     *
     * @param player The admin player.
     * @param page   The page number to display.
     */
    private void openItemGiveGUI(Player player, int page) {
        Inventory itemGUI = Bukkit.createInventory(null, 54, ITEM_GIVE_GUI_TITLE + " - Page " + (page + 1));

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
        if (!event.getView().getTitle().equals(PLUGIN_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("Close")) {
            player.closeInventory();
            return;
        }

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        String pluginName = displayName;
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            player.sendMessage(ChatColor.RED + "Plugin not found.");
            return;
        }

        if (plugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            player.sendMessage(ChatColor.GREEN + "Plugin " + pluginName + " has been disabled.");
        } else {
            Bukkit.getPluginManager().enablePlugin(plugin);
            player.sendMessage(ChatColor.GREEN + "Plugin " + pluginName + " has been enabled.");
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
        String rawTitle = ChatColor.stripColor(event.getView().getTitle());

        if (!rawTitle.startsWith(ChatColor.stripColor(PLAYER_SELECTOR_TITLE)))
            return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player admin = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta())
            return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("Close")) {
            admin.closeInventory();
            return;
        }

        Material clickedType = clickedItem.getType();
        if (clickedType == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        if (clickedType != Material.PLAYER_HEAD)
            return;

        String targetName = displayName;
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            admin.sendMessage(ChatColor.RED + "Player not found...");
            admin.closeInventory();
            return;
        }

        String action = rawTitle.substring(rawTitle.lastIndexOf('-') + 1).trim(); // Extract action from title

        switch (action) {
            case "Kick Player":
                target.kickPlayer(ChatColor.RED + "You have been kicked by an administrator.");
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been kicked.");
                break;
            case "Ban Player":
                target.kickPlayer(ChatColor.RED + "You have been banned by an administrator.");
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), "Banned by an administrator.", null, admin.getName());
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
                admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been brought to you.");
                target.sendMessage(ChatColor.YELLOW + "You have been teleported by an administrator.");
                break;
            case "Inspect Inventory":
                admin.closeInventory();
                admin.openInventory(target.getInventory());
                admin.sendMessage(ChatColor.GREEN + "Inspecting " + target.getName() + "'s inventory.");
                break;
            default:
                admin.sendMessage(ChatColor.RED + "Invalid action.");
                break;
        }
    }

    /**
     * Toggles mute status for a player.
     *
     * @param target The target player.
     * @param admin  The admin player.
     */
    private void toggleMutePlayer(Player target, Player admin) {
        boolean isMuted = muteList.getOrDefault(target.getUniqueId(), false);
        muteList.put(target.getUniqueId(), !isMuted);

        if (isMuted) {
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been unmuted.");
            target.sendMessage(ChatColor.YELLOW + "You have been unmuted.");
        } else {
            admin.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been muted.");
            target.sendMessage(ChatColor.RED + "You have been muted.");
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
        if (!event.getView().getTitle().equals(TIME_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

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
        if (!event.getView().getTitle().equals(WEATHER_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

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
        if (!event.getView().getTitle().equals(WORLD_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

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
        String title = event.getView().getTitle();

        if (!title.startsWith(ITEM_GIVE_GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

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
