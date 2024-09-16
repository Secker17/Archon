package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class ChatInputHandler implements Listener {

    public static HashMap<UUID, String> expectingWorldName = new HashMap<>();
    public static HashMap<UUID, Boolean> expectingCommand = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

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
        } else if (expectingCommand.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            String command = event.getMessage();
            executeServerCommand(player, command);
            expectingCommand.remove(player.getUniqueId());
        }
    }

    private void createWorld(Player player, String worldName) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            WorldCreator wc = new WorldCreator(worldName);
            World newWorld = wc.createWorld();
            player.sendMessage(ChatColor.GREEN + "World " + worldName + " has been created.");
        });
    }

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

    private void executeServerCommand(Player player, String command) {
        Bukkit.getScheduler().runTask(Archon.getInstance(), () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.sendMessage(ChatColor.GREEN + "Executed command: /" + command);
        });
    }
}
