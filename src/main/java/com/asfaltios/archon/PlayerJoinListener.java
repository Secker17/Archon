package com.asfaltios.archon;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        event.getPlayer().sendMessage(ChatColor.AQUA + "Welcome to the server, " + playerName + "!");
        event.setJoinMessage(ChatColor.GREEN + playerName + " has joined the server!");
    }
}
