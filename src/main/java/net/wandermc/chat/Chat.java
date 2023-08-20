package net.wandermc.chat;

import net.wandermc.chat.commands.*;
import net.wandermc.chat.config.*;
import net.wandermc.chat.listeners.*;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
    private File usersFolder;
    private PlayerManager playerManager;

    public void onEnable() {
        setupUsersFolder();

        this.playerManager = new PlayerManager();

        getCommand("ignore").setExecutor(new IgnoreCommand(this.getServer(), this.getLogger(), this.playerManager));
        getCommand("unignore").setExecutor(new UnignoreCommand(this.getServer(), this.getLogger(), this.playerManager));

        getServer().getPluginManager().registerEvents(new ChatListener(this.playerManager), this);
        getServer().getPluginManager().registerEvents(new ConnectionListeners(this.playerManager), this);
    }

    public void onDisable() {}

    /**
     * Ensures existence of "users" config directory and calls setUsersFolder for YamlPlayer.
     */
    private void setupUsersFolder() {
        // Ensure existence of usersFolder
        this.usersFolder = new File(this.getDataFolder(), "users");
        this.usersFolder.mkdir();
        // Removing this line will cause the universe to collapse
        YamlPlayer.setUsersFolder(this.usersFolder);
    }
}
