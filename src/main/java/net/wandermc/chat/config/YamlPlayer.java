/*
 *    WanderChat: a basic chat enhancements plugin for PaperMC servers.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
     * Reloads the file this instance is shadowing.
     *
     * Must be run after a file is manually modified, otherwise the changes will not be recorded.
     */
    public void reloadFile() {
        this.yamlConfig = YamlConfiguration.loadConfiguration(this.playerFile);
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
