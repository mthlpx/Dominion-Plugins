package me.teux.api;

import me.teux.api.utils.ClassScanner;
import me.teux.api.utils.CooldownManager;
import me.teux.api.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private String prefix = "&a[Dominion API] "; // Green prefix



    // Example usage:  System.out.println(stringUtils.formatar("prefix &eProcessing class: " + clazz.getName()));

    public static void registerCommands(Plugin plugin, String packageName) {
        try {

            List<Class<?>> classes = ClassScanner.getClasses(plugin, packageName);

            for (Class<?> clazz : classes) {
                System.out.println(StringUtils.formatar("&e[Dominion API] Processing class: " + clazz.getName()));
                if (clazz.isAnnotationPresent(CommandInfo.class) && EasyCommand.class.isAssignableFrom(clazz)) {
                    CommandInfo info = clazz.getAnnotation(CommandInfo.class);
                    System.out.println(StringUtils.formatar("&a[Dominion API] Registering command: &f" + info.name()));

                    // Detailed log of command information
                    logCommandDetails(info);

                    EasyCommand commandInstance = (EasyCommand) clazz.newInstance();
                    registerCommand(plugin, info, commandInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logCommandDetails(CommandInfo info) {
        // Display all relevant command information
        System.out.println(StringUtils.formatar("&a[Dominion API] Command: &f" + info.name()));
        System.out.println(StringUtils.formatar("&a[Dominion API] Description: &f" + info.description()));
        System.out.println(StringUtils.formatar("&a[Dominion API] Permission: &f" + (info.permission().isEmpty() ? "&7None" : info.permission())));
        System.out.println(StringUtils.formatar("&a[Dominion API] Cooldown: &f" + info.cooldown() + " &aseconds"));
        System.out.println(StringUtils.formatar("&a[Dominion API] Only for players: &f" + (info.onlyPlayer() ? "&cYes" : "&7No")));

        // Display details about subcommands, if any
        if (info.subCommands().length > 0) {
            System.out.println(StringUtils.formatar("&a[Dominion API] Subcommands:"));
            for (SubCommand sub : info.subCommands()) {
                System.out.println(StringUtils.formatar("  &e- Subcommand: &f" + sub.name()));
                System.out.println(StringUtils.formatar("    &a- Permission: &f" + (sub.permission().isEmpty() ? "&7None" : sub.permission())));
                System.out.println(StringUtils.formatar("    &a- Cooldown: &f" + sub.cooldown() + " &aseconds"));
                System.out.println(StringUtils.formatar("    &a- Only for players: &f" + (sub.onlyPlayer() ? "&cYes" : "&7No")));
            }
        } else {
            System.out.println(StringUtils.formatar("&a[Dominion API] No subcommands registered."));
        }
    }

    private static void registerCommand(Plugin plugin, CommandInfo info, EasyCommand executor) {
        try {
            PluginCommand command = getCommand(info.name(), plugin);
            command.setAliases(Arrays.asList(info.aliases()));
            command.setDescription(info.description());

            command.setExecutor((sender, cmd, label, args) -> {
                try {
                    SubCommand sub = args.length > 0 ? getSubCommand(info, args[0]) : null;

                    // 1. Check onlyPlayer first
                    boolean isOnlyPlayer = info.onlyPlayer();
                    if (sub != null) {
                        isOnlyPlayer = sub.onlyPlayer();
                    }

                    if (isOnlyPlayer && !(sender instanceof Player)) {
                        sender.sendMessage(StringUtils.formatar("&cThis command can only be used by players!"));
                        return true;
                    }

                    // 2. Check specific permission
                    String commandPermission = info.permission();
                    if (args.length == 0 && !commandPermission.isEmpty() && !sender.hasPermission(commandPermission)) {
                        sender.sendMessage(StringUtils.formatar("&cYou do not have permission to use this!"));
                        return true;
                    }

                    // Check if there is a subcommand and, if so, check its permission
                    if (sub != null) {
                        String subPermission = sub.permission();
                        if (!subPermission.isEmpty() && !sender.hasPermission(subPermission)) {
                            sender.sendMessage(StringUtils.formatar("&cYou do not have permission to use this!"));
                            return true;
                        }
                    }

                    // 3. Apply cooldown
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        String fullCommand = info.name();
                        int cooldown = info.cooldown();

                        if (sub != null) {
                            fullCommand += "." + sub.name();
                            cooldown = sub.cooldown() == -1 ? info.cooldown() : sub.cooldown();
                        }

                        if (cooldown > 0) {
                            if (CooldownManager.isOnCooldown(fullCommand, player)) {
                                long remaining = CooldownManager.getRemaining(fullCommand, player) / 1000;
                                sender.sendMessage(StringUtils.formatar("&ePlease wait &6" + remaining + "&e seconds before using again!"));
                                return true;
                            }
                            CooldownManager.setCooldown(fullCommand, player, cooldown);
                        }
                    }

                    // 4. Execute command
                    return executor.onCommand(sender, args);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });

            command.setTabCompleter((sender, cmd, alias, args) -> executor.onTabComplete(sender, args));
            getCommandMap().register(plugin.getName(), command);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SubCommand getSubCommand(CommandInfo info, String arg) {
        return Arrays.stream(info.subCommands())
                .filter(sub -> sub.name().equalsIgnoreCase(arg) ||
                        Arrays.stream(sub.aliases()).anyMatch(a -> a.equalsIgnoreCase(arg)))
                .findFirst()
                .orElse(null);
    }

    private static PluginCommand getCommand(String name, Plugin plugin) throws Exception {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name, plugin);
    }

    private static CommandMap getCommandMap() throws Exception {
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getPluginManager());
        }
        return null;
    }
}
