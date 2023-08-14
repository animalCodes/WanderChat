package net.wandermc.chat;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
    private File usersFolder;
    public void onEnable() {
        setupUsersFolder();
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
        net.wandermc.chat.config.YamlPlayer.setUsersFolder(this.usersFolder);
    }
}
