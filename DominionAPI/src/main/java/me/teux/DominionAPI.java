package me.teux;

import me.teux.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DominionAPI extends JavaPlugin {

    private DominionAPI instance;

    @Override
    public void onEnable() {
        instance = this;

        CommandManager.registerCommands(this, "me.teux.commands");

    }

    @Override
    public void onDisable() {
    }
}
