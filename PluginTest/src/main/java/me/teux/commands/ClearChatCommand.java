package me.teux.commands;

import me.teux.api.CommandInfo;
import me.teux.api.EasyCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "clearchat",
        aliases = {"cc"},
        description = "Limpa o chat"
)
public class ClearChatCommand implements EasyCommand {
    private static final String EMPTY_LINE = " ";

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage(EMPTY_LINE);
            }
        }
    }
}
