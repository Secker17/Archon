package com.asfaltios.archon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Archon extends JavaPlugin {

    private static Archon instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();
        displayStartupMessage();

        if (getCommand("admin") != null) {
            getCommand("admin").setExecutor(new AdminCommand(this));
        } else {
            getLogger().warning("Command 'admin' is missing from plugin.yml.");
        }

        getServer().getPluginManager().registerEvents(new AdminGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatInputHandler(), this);

        getLogger().info("Archon enabled for " + Bukkit.getVersion() + ".");
    }

    @Override
    public void onDisable() {
        getLogger().info("Archon disabled.");
    }

    public static Archon getInstance() {
        return instance;
    }

    private void displayStartupMessage() {
        String[] message = {
                "",
                "§b==============================",
                "§b Archon Admin Suite",
                "§7 Cross-version admin controls",
                "§7 Author: §fAsfaltios Advanced",
                "§7 Server: §f" + Bukkit.getVersion(),
                "§7 Discord: §9https://discord.gg/ESZtT2aDS3",
                "§b==============================",
                ""
        };

        for (String line : message) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }
}
