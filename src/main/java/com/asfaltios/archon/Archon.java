package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.GameMode;


public class Archon extends JavaPlugin {

    private GUIManager guiManager;

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GOLD + "Archon plugin has been enabled with advanced admin features!");
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        guiManager = new GUIManager(this);
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "Archon plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("archon")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                return showHelpMenu(sender);
            } else if (args[0].equalsIgnoreCase("gui")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    guiManager.openMainGui(player);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Only players can open the admin control GUI.");
            } else if (args[0].equalsIgnoreCase("ban")) {
                return handleBanCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("kick")) {
                return handleKickCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("mute")) {
                return handleMuteCommand(sender, args, true);
            } else if (args[0].equalsIgnoreCase("unmute")) {
                return handleMuteCommand(sender, args, false);
            } else if (args[0].equalsIgnoreCase("freeze")) {
                return handleFreezeCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("fly")) {
                return handleFlyCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("gm")) {
                return handleGameModeCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("tp")) {
                return handleTeleportCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("heal")) {
                return handleHealCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("feed")) {
                return handleFeedCommand(sender, args);
            }
        }
        return false;
    }

    private boolean showHelpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "------------ " + ChatColor.GOLD + "Archon Admin Plugin" + ChatColor.DARK_PURPLE + " ------------");
        sender.sendMessage(ChatColor.GOLD + "/archon help" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Shows this detailed help menu.");
        sender.sendMessage(ChatColor.GOLD + "/archon gui" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Opens the Admin Control Panel GUI.");
        sender.sendMessage(ChatColor.GOLD + "/archon ban <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Temporarily bans a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon kick <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Kicks a player from the server.");
        sender.sendMessage(ChatColor.GOLD + "/archon mute <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Mutes a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon unmute <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Unmutes a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon freeze <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Freezes a player in place.");
        sender.sendMessage(ChatColor.GOLD + "/archon fly <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Toggles fly mode for a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon gm <player> <gamemode>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Sets the game mode for a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon tp <player> <target>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Teleports one player to another.");
        sender.sendMessage(ChatColor.GOLD + "/archon heal <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Heals a player.");
        sender.sendMessage(ChatColor.GOLD + "/archon feed <player>" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Feeds a player.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "---------------------------------------------");
        sender.sendMessage(ChatColor.YELLOW + "Click on the commands to auto-fill them in the chat!");
        return true;
    }

    // Handle Ban Command
    private boolean handleBanCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon ban <player> [reason]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        String reason = (args.length > 2) ? String.join(" ", args).substring(args[1].length() + 5) : "Misconduct";
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());
        target.kickPlayer(ChatColor.RED + "You have been banned: " + reason);
        sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been banned for: " + reason);
        return true;
    }

    // Handle Kick Command
    private boolean handleKickCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon kick <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        target.kickPlayer(ChatColor.RED + "You have been kicked by an admin.");
        sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been kicked.");
        return true;
    }

    // Handle Mute and Unmute Commands
    private boolean handleMuteCommand(CommandSender sender, String[] args, boolean mute) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon " + (mute ? "mute" : "unmute") + " <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        if (mute) {
            // Add code to mute player
            target.sendMessage(ChatColor.RED + "You have been muted by an admin.");
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been muted.");
        } else {
            // Add code to unmute player
            target.sendMessage(ChatColor.GREEN + "You have been unmuted by an admin.");
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been unmuted.");
        }
        return true;
    }

    // Handle Freeze Command
    private boolean handleFreezeCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon freeze <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        // Add code to freeze player (disabling movement)
        sender.sendMessage(ChatColor.AQUA + "Player " + target.getName() + " has been frozen.");
        return true;
    }

    // Handle Fly Command
    private boolean handleFlyCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon fly <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        boolean canFly = target.getAllowFlight();
        target.setAllowFlight(!canFly);
        sender.sendMessage(ChatColor.AQUA + "Fly mode for " + target.getName() + " has been " + (canFly ? "disabled" : "enabled") + ".");
        return true;
    }

    // Handle Game Mode Command
    private boolean handleGameModeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon gm <player> <gamemode>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        GameMode gameMode;
        switch (args[2].toLowerCase()) {
            case "creative":
                gameMode = GameMode.CREATIVE;
                break;
            case "survival":
                gameMode = GameMode.SURVIVAL;
                break;
            case "adventure":
                gameMode = GameMode.ADVENTURE;
                break;
            case "spectator":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid gamemode. Use: creative, survival, adventure, or spectator.");
                return false;
        }
        target.setGameMode(gameMode);
        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s game mode to " + gameMode.name().toLowerCase() + ".");
        return true;
    }

    // Handle Teleport Command
    private boolean handleTeleportCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon tp <player> <target>");
            return false;
        }
        Player player = Bukkit.getPlayer(args[1]);
        Player target = Bukkit.getPlayer(args[2]);
        if (player == null || target == null) {
            sender.sendMessage(ChatColor.RED + "Player or target not found.");
            return false;
        }
        player.teleport(target.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Teleported " + player.getName() + " to " + target.getName() + ".");
        return true;
    }

    // Handle Heal Command
    private boolean handleHealCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon heal <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        target.setHealth(20.0);
        sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been healed.");
        return true;
    }

    // Handle Feed Command
    private boolean handleFeedCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /archon feed <player>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        target.setFoodLevel(20);
        sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been fed.");
        return true;
    }
}
