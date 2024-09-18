package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Archon extends JavaPlugin {

    private static Archon instance;

    @Override
    public void onEnable() {
        instance = this;

        // Display a fancy startup message in the console
        displayStartupMessage();

        // Register commands and event listeners
        getCommand("admin").setExecutor(new AdminCommand(this));
        getServer().getPluginManager().registerEvents(new AdminGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatInputHandler(), this);

        getLogger().info("Archon plugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Archon plugin disabled.");
    }

    /**
     * Get instance of the plugin.
     *
     * @return The instance of Archon plugin.
     */
    public static Archon getInstance() {
        return instance;
    }

    /**
     * Displays a fancy and modern startup message in the console.
     */
    private void displayStartupMessage() {
        String[] message = {
                "",
                "§a███████  ███████╗   ",
                "§a██╔══██  ██╔════╝   ",
                "§a███████  █████╗ ",
                "§a██╔══██  ██╔══╝  ",
                "§a██╔═ ██  ██║     ",
                "§a╚═╝  ╚╝  ╚═╝     ",
                "",
                "§eMade by Asfaltios Advanced",
                "§eJoin our Discord: §9https://discord.gg/ESZtT2aDS3",
                ""
        };

        for (String line : message) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }
}
