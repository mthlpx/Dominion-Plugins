package me.teux;

import me.teux.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginTest extends JavaPlugin {
    private PluginTest instance;

    @Override
    public void onEnable() {
        instance = this;

        CommandManager.registerCommands(this, "me.teux.commands");
    }

    @Override
    public void onDisable() {
    }

    public PluginTest getInstance() {
        return instance;
    }
}
