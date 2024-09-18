package com.asfaltios.archon;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AdminCommand implements CommandExecutor {

    private final Plugin plugin;

    public AdminCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("archon.admin.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload the plugin.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Archon plugin reloaded.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("archon.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        AdminGUI.openMainGUI(player);
        return true;
    }
}
