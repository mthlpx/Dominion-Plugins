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
            List<Class<?>> classes = ClassScanner.getClasses(plugin, packageName);

            for (Class<?> clazz : classes) {
                System.out.println("[Dominion API] Processando classe: " + clazz.getName());
                if (clazz.isAnnotationPresent(CommandInfo.class) && EasyCommand.class.isAssignableFrom(clazz)) {
                    CommandInfo info = clazz.getAnnotation(CommandInfo.class);
                    System.out.println("[Dominion API] Registrando comando: " + info.name());
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

            command.setExecutor((sender, cmd, label, args) -> {
                try {
                    SubCommand sub = args.length > 0 ? getSubCommand(info, args[0]) : null;

                    // 1. Verificar onlyPlayer primeiro
                    boolean isOnlyPlayer = info.onlyPlayer();
                    if (sub != null) {
                        isOnlyPlayer = sub.onlyPlayer();
                    }

                    if (isOnlyPlayer && !(sender instanceof Player)) {
                        sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
                        return true;
                    }

                    // 2. Verificar permissão específica
                    String commandPermission = info.permission();
                    if (args.length == 0 && !commandPermission.isEmpty() && !sender.hasPermission(commandPermission)) {
                        sender.sendMessage("§cVocê não tem permissão para executar o comando principal!");
                        return true;
                    }

                    // Verificar se existe um subcomando e, se sim, verificar sua permissão
                    if (sub != null) {
                        String subPermission = sub.permission();
                        if (!subPermission.isEmpty() && !sender.hasPermission(subPermission)) {
                            sender.sendMessage("§cVocê não tem permissão para executar o subcomando!");
                            return true;
                        }
                    }

                    // 3. Aplicar cooldown
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
                                sender.sendMessage("§eAguarde §6" + remaining + "§e segundos para usar novamente!");
                                return true;
                            }
                            CooldownManager.setCooldown(fullCommand, player, cooldown);
                        }
                    }

                    // 4. Executar comando
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