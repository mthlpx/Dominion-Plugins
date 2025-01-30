package me.teux.api;

import me.teux.api.utils.ClassScanner;
import me.teux.api.utils.CooldownManager;
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

    public static void registerCommands(Plugin plugin, String packageName) {
        try {
            System.out.println("[DEBUG] Procurando comandos em: " + packageName);
            List<Class<?>> classes = ClassScanner.getClasses(plugin, packageName);
            System.out.println("[DEBUG] Total de classes encontradas: " + classes.size());

            for (Class<?> clazz : classes) {
                System.out.println("[DEBUG] Processando classe: " + clazz.getName());
                if (clazz.isAnnotationPresent(CommandInfo.class) && EasyCommand.class.isAssignableFrom(clazz)) {
                    CommandInfo info = clazz.getAnnotation(CommandInfo.class);
                    System.out.println("[DEBUG] Registrando comando: " + info.name());
                    EasyCommand commandInstance = (EasyCommand) clazz.newInstance();
                    registerCommand(plugin, info, commandInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerCommand(Plugin plugin, CommandInfo info, EasyCommand executor) {
        try {
            PluginCommand command = getCommand(info.name(), plugin);
            command.setAliases(Arrays.asList(info.aliases()));
            command.setDescription(info.description());
            command.setPermission(info.permission());

            // Configura executor com tratamento de subcomandos
            command.setExecutor((sender, cmd, label, args) -> {
                try {
                    SubCommand sub = args.length > 0 ? getSubCommand(info, args[0]) : null;

                    String requiredPermission = info.permission();
                    if (sub != null && !sub.permission().isEmpty()) {
                        requiredPermission = sub.permission();
                    }

                    if (!requiredPermission.isEmpty() && !sender.hasPermission(requiredPermission)) {
                        sender.sendMessage("§cVocê não tem permissão!");
                        return true;
                    }

                    boolean isOnlyPlayer = info.onlyPlayer();
                    if (sub != null) {
                        isOnlyPlayer = sub.onlyPlayer();
                    }

                    if (isOnlyPlayer && !(sender instanceof Player)) {
                        sender.sendMessage("§cEste comando somente pode ser usado por jogadores!");
                        return true;
                    }

                    // Verificar cooldown
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        String fullCommand = info.name() + (args.length > 0 ? "." + args[0] : "");
                        int cooldown = getEffectiveCooldown(info, args);

                        if (sub != null) {
                            fullCommand += "." + sub.name();
                            cooldown = sub.cooldown() == -1 ? info.cooldown() : sub.cooldown();
                        }

                        if (cooldown > 0 && CooldownManager.isOnCooldown(fullCommand, player)) {
                            long remaining = CooldownManager.getRemaining(fullCommand, player) / 1000;
                            sender.sendMessage("§eAguarde §6" + remaining + "§e segundos para usar novamente!");
                            return true;
                        }

                        if (cooldown > 0) {
                            CooldownManager.setCooldown(fullCommand, player, cooldown);
                        }
                    }

                    // Executar comando
                    return executor.onCommand(sender, args);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });

            // Tab completer
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

    private static void registerSubCommands(Plugin plugin, CommandInfo info, EasyCommand executor) {
        for (SubCommand sub : info.subCommands()) {
            try {
                PluginCommand subCommand = getCommand(info.name() + ":" + sub.name(), plugin);
                subCommand.setAliases(Arrays.asList(sub.aliases()));
                subCommand.setPermission(sub.permission().isEmpty() ? info.permission() : sub.permission());

                subCommand.setExecutor((sender, cmd, label, args) -> {
                    // Lógica similar ao comando principal
                    SubCommand suba = args.length > 0 ? getSubCommand(info, args[0]) : null;

                    return executor.onCommand(sender, args);
                });

                getCommandMap().register(plugin.getName(), subCommand);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean matchesSubCommand(SubCommand sub, String arg) {
        return sub.name().equalsIgnoreCase(arg) ||
                Arrays.stream(sub.aliases()).anyMatch(a -> a.equalsIgnoreCase(arg));
    }

    private static int getEffectiveCooldown(CommandInfo info, String[] args) {
        if (args.length == 0) return info.cooldown();

        for (SubCommand sub : info.subCommands()) {
            if (matchesSubCommand(sub, args[0])) {
                return sub.cooldown() == -1 ? info.cooldown() : sub.cooldown();
            }
        }
        return info.cooldown();
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