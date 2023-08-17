package net.wandermc.chat.config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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
     * Sets the static variable usersFolder, must be run before any instances of
     * this class are made.
     * 
     * @param folder The "users" folder in this plugin's dataFolder
     */
    public static void setUsersFolder(File folder) {
        usersFolder = folder;
    }

    /**
     * Locates and returns the file for user with UUID `this.uuid`, creating the
     * file if it doesn't exist.
     * 
     * @param usersFolder The /users directory in this plugin's dataFolder
     */
    public File getPlayerFile(File usersFolder) {
        File file = new File(usersFolder, this.uuid + ".yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        return file;
    }

    /**
     * Attempts to save the yaml data currently held by this object to the player's
     * file.
     * 
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
     * Gets this player's (last stored) username.
     * 
     * @return The username stored in the file represented by this instance
     */
    public String getUsername() {
        return this.yamlConfig.getString("username");
    }

    /**
     * Sets "username"
     */
    public void setUsername(String path) {
        this.yamlConfig.set("username", path);
    }

    /**
     * Gets the list of players "ignored" by this player.
     * 
     * @return The UUIDs of all players currently ignored by this player
     */
    public List<UUID> getIgnored() {
        ArrayList<UUID> uuidList = new ArrayList<>();
        this.yamlConfig.getStringList("ignored").forEach(
                item -> {
                    uuidList.add(UUID.fromString(item));
                });
        return uuidList;
    }

    /**
     * Sets the UUIDs of the player's currently ignored by this player.
     *
     * Note that this overwrites the previous value, for appending `appendIgnored` can be used.
     * 
     * @param ignoredList The new value of "ignored"
     */
    public void setIgnored(List<UUID> ignoredList) {
        ArrayList<String> stringList = new ArrayList<>(ignoredList.size());
        ignoredList.forEach(
                item -> {
                    stringList.add(item.toString());
                });
        this.yamlConfig.set("ignored", stringList);
    }

    /**
     * Appends a single uuid to "ignored".
     *
     * @param uuid UUID to append
     */
    public void appendIgnored(UUID uuid) {
        ArrayList<UUID> ignored = (ArrayList<UUID>)this.getIgnored();
        ignored.add(uuid);
        this.setIgnored(ignored);
    }

    /**
     * Removes a single uuid from "ignored".
     *
     * @param uuid UUID to remove
     */
    public void removeIgnored(UUID uuid) {
        ArrayList<UUID> ignored = (ArrayList<UUID>)this.getIgnored();
        ignored.remove(uuid);
        this.setIgnored(ignored);
    }

    /**
     * Checks if `uuid` is ignored by this player.
     *
     * @param uuid UUID to check
     */
    public boolean isIgnoring(UUID uuid) {
        return this.getIgnored().contains(uuid);
    }
}
