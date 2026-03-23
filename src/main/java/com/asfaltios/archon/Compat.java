package com.asfaltios.archon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class Compat {

    private Compat() {
    }

    public static Material material(String... names) {
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            try {
                Material material = Material.valueOf(name);
                if (material != null) {
                    return material;
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                Material material = Material.matchMaterial(name);
                if (material != null) {
                    return material;
                }
            } catch (NoSuchMethodError ignored) {
            }
        }

        return Material.STONE;
    }

    public static Material configuredMaterial(String configuredName, Material fallback, String... legacyFallbacks) {
        Material configured = material(configuredName);
        if (isUsableItem(configured)) {
            return configured;
        }

        if (legacyFallbacks != null && legacyFallbacks.length > 0) {
            Material legacy = material(legacyFallbacks);
            if (isUsableItem(legacy)) {
                return legacy;
            }
        }

        return isUsableItem(fallback) ? fallback : Material.STONE;
    }

    public static boolean isUsableItem(Material material) {
        if (material == null || material == Material.AIR) {
            return false;
        }

        try {
            Method isItemMethod = Material.class.getMethod("isItem");
            Object result = isItemMethod.invoke(material);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    public static ItemStack playerHead(OfflinePlayer owner, String name, List<String> lore) {
        ItemStack skull = new ItemStack(material("PLAYER_HEAD", "SKULL_ITEM"), 1);

        if ("SKULL_ITEM".equals(skull.getType().name())) {
            try {
                Method setDurability = ItemStack.class.getMethod("setDurability", short.class);
                setDurability.invoke(skull, (short) 3);
            } catch (Exception ignored) {
            }
        }

        ItemMeta rawMeta = skull.getItemMeta();
        if (!(rawMeta instanceof SkullMeta)) {
            return skull;
        }

        SkullMeta meta = (SkullMeta) rawMeta;
        setSkullOwner(meta, owner);
        meta.setDisplayName(name);
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    public static void setSkullOwner(SkullMeta meta, OfflinePlayer owner) {
        if (meta == null || owner == null) {
            return;
        }

        try {
            Method method = meta.getClass().getMethod("setOwningPlayer", OfflinePlayer.class);
            method.invoke(meta, owner);
            return;
        } catch (Exception ignored) {
        }

        if (owner.getName() == null) {
            return;
        }

        try {
            Method method = meta.getClass().getMethod("setOwner", String.class);
            method.invoke(meta, owner.getName());
        } catch (Exception ignored) {
        }
    }

    public static void hidePlayer(Player viewer, Player target, Plugin plugin) {
        if (viewer == null || target == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("hidePlayer", Plugin.class, Player.class);
            method.invoke(viewer, plugin, target);
            return;
        } catch (Exception ignored) {
        }

        try {
            Method method = Player.class.getMethod("hidePlayer", Player.class);
            method.invoke(viewer, target);
        } catch (Exception ignored) {
        }
    }

    public static void showPlayer(Player viewer, Player target, Plugin plugin) {
        if (viewer == null || target == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("showPlayer", Plugin.class, Player.class);
            method.invoke(viewer, plugin, target);
            return;
        } catch (Exception ignored) {
        }

        try {
            Method method = Player.class.getMethod("showPlayer", Player.class);
            method.invoke(viewer, target);
        } catch (Exception ignored) {
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            method.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            return;
        } catch (Exception ignored) {
        }

        try {
            Method method = Player.class.getMethod("sendTitle", String.class, String.class);
            method.invoke(player, title, subtitle);
        } catch (Exception ignored) {
        }
    }

    public static void playSound(Player player, Location location, float volume, float pitch, String... soundNames) {
        if (player == null || location == null) {
            return;
        }

        Object sound = enumConstant("org.bukkit.Sound", soundNames);
        if (sound == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("playSound", Location.class, sound.getClass(), float.class, float.class);
            method.invoke(player, location, sound, volume, pitch);
        } catch (Exception ignored) {
        }
    }

    public static void spawnParticle(Player player, Location location, int count, double offsetX, double offsetY, double offsetZ, String... particleNames) {
        if (player == null || location == null) {
            return;
        }

        Object particle = enumConstant("org.bukkit.Particle", particleNames);
        if (particle == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("spawnParticle", particle.getClass(), Location.class, int.class, double.class, double.class, double.class);
            method.invoke(player, particle, location, count, offsetX, offsetY, offsetZ);
        } catch (Exception ignored) {
        }
    }

    public static double getMaxHealth(Player player) {
        if (player == null) {
            return 20.0D;
        }

        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            Field genericMaxHealth = attributeClass.getField("GENERIC_MAX_HEALTH");
            Method getAttribute = Player.class.getMethod("getAttribute", attributeClass);
            Object attributeInstance = getAttribute.invoke(player, genericMaxHealth.get(null));
            if (attributeInstance != null) {
                Method getValue = attributeInstance.getClass().getMethod("getValue");
                Object value = getValue.invoke(attributeInstance);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Method getMaxHealth = Player.class.getMethod("getMaxHealth");
            Object value = getMaxHealth.invoke(player);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        } catch (Exception ignored) {
        }

        return 20.0D;
    }

    public static void heal(Player player) {
        if (player == null) {
            return;
        }

        double maxHealth = getMaxHealth(player);
        player.setHealth(maxHealth);
    }

    private static Object enumConstant(String className, String... names) {
        try {
            Class<?> enumClass = Class.forName(className);
            for (String name : names) {
                if (name == null || name.trim().isEmpty()) {
                    continue;
                }

                try {
                    return Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
