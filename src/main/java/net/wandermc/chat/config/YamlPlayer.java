package net.wandermc.chat.config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class YamlPlayer {
    // TODO test whether this is null
    private static File usersFolder;

    private UUID uuid;
    private File playerFile;
    private YamlConfiguration yamlConfig;

    public YamlPlayer(UUID uuid) {
        this.uuid = uuid;
        this.playerFile = getPlayerFile(usersFolder);
        this.yamlConfig = YamlConfiguration.loadConfiguration(this.playerFile);
    }

    /**
     * Sets the static variable usersFolder, must be run before any instances of this class are made.
     * 
     * @param folder The "users" folder in this plugin's dataFolder
     */
    public static void setUsersFolder(File folder) {
        usersFolder = folder;
    }

    /**
     * Locates and returns the file for user with UUID `this.uuid`, creating the file if it doesn't exist.
     * @param usersFolder The /users directory in this plugin's dataFolder
     */
    public File getPlayerFile(File usersFolder) {
        File file = new File(usersFolder, this.uuid+".yml");
        try {file.createNewFile();} 
        catch (IOException e) {}
        return file;
    }

    /**
     * Attempts to save the yaml data currently held by this object to the player's file.
     * @return Whether saving was successful
     */
    public boolean save() {
        try {
            this.yamlConfig.save(this.playerFile);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the boolean at `path`.
     * @return The value of `path` or null if it doesn't exist
     */
    public boolean getBoolean(String path) {
        return this.yamlConfig.getBoolean(path);
    }

    /**
     * Gets the double at `path`.
     * @return The value of `path` or null if it doesn't exist
     */
    public double getDouble(String path) {
        return this.yamlConfig.getDouble(path);
    }

    /**
     * Gets the int at `path`.
     * @return The value of `path` or null if it doesn't exist
     */
    public int getInt(String path) {
        return this.yamlConfig.getInt(path);
    }

    /**
     * Gets the String at `path`.
     * @return The value of `path` or null if it doesn't exist
     */
    public String getString(String path) {
        return this.yamlConfig.getString(path);
    }

    /**
     * Sets `path` to `value`.
     *
     * Note that this does **not** save stored data to file, this must be done manually with `save()`
     *
     * @param path The path at which to store `value`
     * @param value The value to store, can be of any type
     */
    public void set(String path, Object value) {
        this.yamlConfig.set(path, value);
    }
}

