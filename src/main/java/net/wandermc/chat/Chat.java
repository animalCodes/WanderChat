package net.wandermc.chat;

import net.wandermc.chat.commands.IgnoreCommand;
import net.wandermc.chat.config.YamlPlayer;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;

public class Chat extends JavaPlugin {
    private File usersFolder;

    public void onEnable() {
        setupUsersFolder();

        getCommand("ignore").setExecutor(new IgnoreCommand(this.getServer(), this.getLogger()));
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
