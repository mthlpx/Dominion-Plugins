package me.teux.api.utils;

import org.bukkit.ChatColor;

public class StringUtils {

    public StringUtils() {
    }

    public static String formatar(String mensagem) {
        return ChatColor.translateAlternateColorCodes('&', mensagem);
    }

    public static String negrito(String mensagem) {
        return ChatColor.BOLD + mensagem + ChatColor.RESET;
    }

    public static String italico(String mensagem) {
        return ChatColor.ITALIC + mensagem + ChatColor.RESET;
    }

    public static String sublinhado(String mensagem) {
        return ChatColor.UNDERLINE + mensagem + ChatColor.RESET;
    }

    public static String riscado(String mensagem) {
        return ChatColor.STRIKETHROUGH + mensagem + ChatColor.RESET;
    }

    public static String formatarPlaceholders(String mensagem, String placeholder, String valor) {
        return mensagem.replace(placeholder, valor);
    }
}
