package com.asfaltios.archon;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ServerUtils {

    public static double getTPS() {
        try {
            // Use reflection to fetch TPS for older versions
            Object minecraftServer = getMinecraftServer();
            Field recentTpsField = minecraftServer.getClass().getField("recentTps");
            recentTpsField.setAccessible(true);
            double[] recentTps = (double[]) recentTpsField.get(minecraftServer);
            return recentTps[0]; // Return the first value as the current TPS
        } catch (Exception ex) {
            // If all else fails, return 20.0 as a fallback
            return 20.0;
        }
    }

    private static Object getMinecraftServer() {
        try {
            Method getServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer");
            getServer.setAccessible(true);
            return getServer.invoke(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
