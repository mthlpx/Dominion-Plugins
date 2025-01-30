package me.teux.api.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private static final HashMap<String, HashMap<UUID, Long>> cooldowns = new HashMap<>();

    public static void setCooldown(String command, Player player, int seconds) {
        if (!cooldowns.containsKey(command)) {
            cooldowns.put(command, new HashMap<>());
        }
        cooldowns.get(command).put(player.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public static long getRemaining(String command, Player player) {
        return cooldowns.getOrDefault(command, new HashMap<>())
                .getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
    }

    public static boolean isOnCooldown(String command, Player player) {
        return getRemaining(command, player) > 0;
    }
}