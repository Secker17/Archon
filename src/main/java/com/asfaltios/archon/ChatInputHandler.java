package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.attribute.Attribute;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ChatInputHandler implements Listener {

    // HashMaps to track players expecting input for various actions
    public static HashMap<UUID, String> expectingWorldName = new HashMap<>();
    public static HashMap<UUID, Boolean> expectingCommand = new HashMap<>();
    public static HashMap<UUID, String> expectingWhitelistPlayer = new HashMap<>();
    public static HashMap<UUID, Boolean> expectingSpeedValue = new HashMap<>();
    public static HashMap<UUID, Boolean> expectingLogPage = new HashMap<>();
    public static HashMap<UUID, UUID> expectingHealthValue = new HashMap<>();
    public static HashMap<UUID, UUID> expectingPrivateMessage = new HashMap<>();
    public static HashMap<UUID, SimpleEntry<UUID, Boolean>> expectingPermissionNode = new HashMap<>();
    public static HashMap<UUID, UUID> permissionTargets = new HashMap<>();
    public static HashMap<UUID, Boolean> expectingShutdownTime = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Handle world-related inputs
        if (expectingWorldName.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String action = expectingWorldName.get(player.getUniqueId());
            String worldName = event.getMessage();

            switch (action) {
                case "create":
                    createWorld(player, worldName);
                    break;
                case "delete":
                    deleteWorld(player, worldName);
                    break;
                case "teleport":
                    teleportToWorld(player, worldName);
                    break;
                default:
                    break;
            }

            expectingWorldName.remove(player.getUniqueId());
        }
        // Handle command execution input
        else if (expectingCommand.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            String command = event.getMessage();
            executeServerCommand(player, command);
            expectingCommand.remove(player.getUniqueId());
        }
        // Handle whitelist management input
        else if (expectingWhitelistPlayer.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String action = expectingWhitelistPlayer.get(player.getUniqueId());
            String playerName = event.getMessage();

            switch (action) {
                case "add":
                    addPlayerToWhitelist(player, playerName);
                    break;
                default:
                    break;
            }

            expectingWhitelistPlayer.remove(player.getUniqueId());
        }
        // Handle speed setting input
        else if (expectingSpeedValue.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            String speedInput = event.getMessage();
            setPlayerSpeed(player, speedInput);
            expectingSpeedValue.remove(player.getUniqueId());
        }
        // Handle log viewing input
        else if (expectingLogPage.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            String pageInput = event.getMessage();

            if (pageInput.equalsIgnoreCase("exit")) {
                player.sendMessage(ChatColor.GREEN + "Exited log viewing.");
                expectingLogPage.remove(player.getUniqueId());
                return;
            }

            viewLogs(player, pageInput);
            // Do not remove expectingLogPage here to allow continuous viewing
        }
        // Handle setting health value
        else if (expectingHealthValue.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            UUID targetUUID = expectingHealthValue.get(player.getUniqueId());
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null) {
                try {
                    double health = Double.parseDouble(event.getMessage());
                    double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    if (health < 1 || health > maxHealth) {
                        player.sendMessage(ChatColor.RED + "Health must be between 1 and " + maxHealth + ".");
                    } else {
                        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
                            target.setHealth(health);
                            target.sendMessage(ChatColor.GREEN + "Your health has been set to " + health + " by an administrator.");
                            player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s health to " + health + ".");
                        });
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number format.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Player not found.");
            }
            expectingHealthValue.remove(player.getUniqueId());
        }
        // Handle private messaging
        else if (expectingPrivateMessage.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            UUID targetUUID = expectingPrivateMessage.get(player.getUniqueId());
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null) {
                String message = event.getMessage();
                Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
                    target.sendMessage(ChatColor.LIGHT_PURPLE + "[Private] " + player.getName() + ": " + ChatColor.WHITE + message);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "[Private] To " + target.getName() + ": " + ChatColor.WHITE + message);
                });
            } else {
                player.sendMessage(ChatColor.RED + "Player not found.");
            }
            expectingPrivateMessage.remove(player.getUniqueId());
        }
        // Handle permission management
        else if (expectingPermissionNode.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            SimpleEntry<UUID, Boolean> entry = expectingPermissionNode.get(player.getUniqueId());
            UUID targetUUID = entry.getKey();
            boolean isAdding = entry.getValue();
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null) {
                String permissionNode = event.getMessage();
                Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
                    PermissionAttachment attachment = target.addAttachment(Archon.getInstance());
                    if (isAdding) {
                        attachment.setPermission(permissionNode, true);
                        target.recalculatePermissions();
                        player.sendMessage(ChatColor.GREEN + "Added permission '" + permissionNode + "' to " + target.getName() + ".");
                        target.sendMessage(ChatColor.YELLOW + "An administrator has granted you permission: " + permissionNode);
                    } else {
                        attachment.unsetPermission(permissionNode);
                        target.recalculatePermissions();
                        player.sendMessage(ChatColor.GREEN + "Removed permission '" + permissionNode + "' from " + target.getName() + ".");
                        target.sendMessage(ChatColor.YELLOW + "An administrator has revoked your permission: " + permissionNode);
                    }
                });
            } else {
                player.sendMessage(ChatColor.RED + "Player not found.");
            }
            expectingPermissionNode.remove(player.getUniqueId());
        }
        // Handle shutdown time input
        else if (expectingShutdownTime.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            String timeInput = event.getMessage();
            try {
                int time = Integer.parseInt(timeInput);
                if (time <= 0) {
                    player.sendMessage(ChatColor.RED + "Time must be a positive integer.");
                    return;
                }
                initiateServerShutdown(player, time);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number format.");
            }
            expectingShutdownTime.remove(player.getUniqueId());
        }
    }

    private void initiateServerShutdown(Player player, int timeInSeconds) {
        Bukkit.broadcastMessage(ChatColor.RED + "Server is stopping in " + timeInSeconds + " seconds!");
        Bukkit.getScheduler().runTaskLater(Archon.getInstance(), () -> {
            Bukkit.shutdown();
        }, timeInSeconds * 20L); // Convert seconds to ticks
    }


    /**
     * Creates a new world with the given name.
     *
     * @param player    The admin player.
     * @param worldName The name of the world to create.
     */
    private void createWorld(Player player, String worldName) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            WorldCreator wc = new WorldCreator(worldName);
            World newWorld = wc.createWorld();
            if (newWorld != null) {
                player.sendMessage(ChatColor.GREEN + "World " + worldName + " has been created.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to create world " + worldName + ".");
            }
        });
    }

    /**
     * Deletes an existing world with the given name.
     *
     * @param player    The admin player.
     * @param worldName The name of the world to delete.
     */
    private void deleteWorld(Player player, String worldName) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                for (Player p : world.getPlayers()) {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    p.sendMessage(ChatColor.RED + "World is being deleted, you have been moved to spawn.");
                }
                Bukkit.unloadWorld(world, false);

                File worldFolder = world.getWorldFolder();
                deleteWorldFolder(worldFolder);

                player.sendMessage(ChatColor.GREEN + "World " + worldName + " has been deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "World " + worldName + " does not exist.");
            }
        });
    }

    /**
     * Recursively deletes the world folder.
     *
     * @param path The file path of the world folder.
     */
    private void deleteWorldFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteWorldFolder(f);
                    } else {
                        f.delete();
                    }
                }
            }
            path.delete();
        }
    }

    /**
     * Teleports the admin player to the specified world.
     *
     * @param player    The admin player.
     * @param worldName The name of the world to teleport to.
     */
    private void teleportToWorld(Player player, String worldName) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                player.teleport(world.getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "Teleported to world " + worldName + ".");
            } else {
                player.sendMessage(ChatColor.RED + "World " + worldName + " does not exist.");
            }
        });
    }

    /**
     * Executes a server command on behalf of the admin player.
     *
     * @param player  The admin player.
     * @param command The command to execute.
     */
    private void executeServerCommand(Player player, String command) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Executed command: /" + command);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to execute command: /" + command);
            }
        });
    }

    /**
     * Adds a player to the server whitelist.
     *
     * @param player     The admin player.
     * @param playerName The name of the player to add.
     */
    private void addPlayerToWhitelist(Player player, String playerName) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (target != null) {
                target.setWhitelisted(true);
                player.sendMessage(ChatColor.GREEN + "Player " + playerName + " has been added to the whitelist.");
            } else {
                player.sendMessage(ChatColor.RED + "Player " + playerName + " not found.");
            }
        });
    }

    /**
     * Sets the movement speed of the admin player.
     *
     * @param player     The admin player.
     * @param speedInput The speed value input by the player.
     */
    private void setPlayerSpeed(Player player, String speedInput) {
        try {
            float speed = Float.parseFloat(speedInput);
            if (speed < 0.1f || speed > 10.0f) {
                player.sendMessage(ChatColor.RED + "Speed must be between 0.1 and 10.");
                return;
            }
            float actualSpeed = speed / 10.0f; // Bukkit speed range is from 0.0 to 1.0
            Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
                if (player.isFlying()) {
                    player.setFlySpeed(actualSpeed);
                } else {
                    player.setWalkSpeed(actualSpeed);
                }
                player.sendMessage(ChatColor.GREEN + "Speed set to " + speed + ".");
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Speed value is out of bounds.");
        }
    }

    /**
     * Displays server logs to the admin player in a paginated format.
     *
     * @param player    The admin player.
     * @param pageInput The page number input by the player.
     */
    void viewLogs(Player player, String pageInput) {
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(pageInput);
            if (pageNumber < 1) {
                player.sendMessage(ChatColor.RED + "Page number must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid page number.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Archon.getInstance(), () -> {
            try {
                File logFile = new File("logs/latest.log");
                if (!logFile.exists()) {
                    player.sendMessage(ChatColor.RED + "Log file not found.");
                    return;
                }

                List<String> logLines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
                int linesPerPage = 10;
                int totalLines = logLines.size();
                int totalPages = (totalLines - 1) / linesPerPage + 1;

                if (pageNumber > totalPages) {
                    player.sendMessage(ChatColor.RED + "Page " + pageNumber + " does not exist. Total pages: " + totalPages);
                    return;
                }

                int startIndex = (pageNumber - 1) * linesPerPage;
                int endIndex = Math.min(startIndex + linesPerPage, totalLines);

                player.sendMessage(ChatColor.GREEN + "Server Logs - Page " + pageNumber + "/" + totalPages);
                for (int i = startIndex; i < endIndex; i++) {
                    player.sendMessage(ChatColor.GRAY + logLines.get(i));
                }

                player.sendMessage(ChatColor.YELLOW + "Type the page number to view another page or 'exit' to close.");
                expectingLogPage.put(player.getUniqueId(), true);

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "An error occurred while reading the log file.");
                e.printStackTrace();
            }
        });
    }
}
