package me.teux.commands;

import me.teux.api.CommandInfo;
import me.teux.api.EasyCommand;
import me.teux.api.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        name = "test",
        aliases = {"t", "testing"},
        onlyPlayer = true,
        cooldown = 10,
        description = "Comando de Teste",
        permission = "plugintest.admin",
        subCommands = {
                @SubCommand(
                        name = "sub",
                        cooldown = 20,
                        aliases = {"s"},
                        onlyPlayer = false
                ),
                @SubCommand(
                        name = "info",
                        permission = "plugintest.test.info",
                        cooldown = 30,
                        aliases = {"i"},
                        onlyPlayer = true
                )
        }
)
public class TestCommand implements EasyCommand {
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "sub":
                    sender.sendMessage("§aSub Comando Executado");
                    return;
                case "info":
                    sender.sendMessage("§aSub Comando Info Executado");
                    return;
            }
        }
        sender.sendMessage("§aComando principal executado!");
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        tab.add("sub");
        tab.add("info");

        if (args.length == 1) {
            return tab;
        }
        return new ArrayList<>();
    }
}
