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

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin-prefix"));

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(plugin.getConfig().getString("permissions.admin-reload"))) {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(prefix + ChatColor.GREEN + "Archon plugin reloaded.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(plugin.getConfig().getString("permissions.admin-command"))) {
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        AdminGUI.openMainGUI(player);
        return true;
    }
}

