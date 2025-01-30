package me.teux.api;

import org.bukkit.command.CommandSender;

public interface EasyCommand {
    void execute(CommandSender sender, String[] args);

    default boolean onCommand(CommandSender sender, String[] args) {
        execute(sender, args);
        return true;
    }

    default java.util.List<String> onTabComplete(CommandSender sender, String[] args) {
        return new java.util.ArrayList<>();
    }
}