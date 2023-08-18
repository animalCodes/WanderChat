package net.wandermc.chat.config;

import java.util.HashMap;
import java.util.UUID;

import net.wandermc.chat.config.YamlPlayer;

public class PlayerManager {
    private HashMap<UUID, YamlPlayer> players;

    public PlayerManager() {}

    /**
     * Loads a YamlPlayer object for player `uuid`, also used for updating an entry.
     *
     * @param uuid The uuid representing the YamlPlayer instance to load
     * @return Whether the YamlPlayer was already loaded
     */
    public boolean loadYamlPlayer(UUID uuid) {
        return this.players.put(uuid, new YamlPlayer(uuid)) != null;
    }

    /**
     * Unloads a the YamlPlayer instance associated with `uuid`.
     *
     * @param uuid The uuid representing the YamlPlayer instance to remove
     * @return Whether the YamlPlayer was loaded
     */
    public boolean unloadYamlPlayer(UUID uuid) {
        return this.players.remove(uuid) != null;
    }

    /**
     * Attempts to retrieve the YamlPlayer instance associated with `uuid`.
     *
     * If `tryLoad` is true and the instance isn't loaded, an attempt will be made to load the appropriate object.
     *
     * @param uuid The uuid representing the YamlPlayer instance to get
     * @param tryLoad Whether an attempt should be made to load the YamlPlayer instance if it isn't loaded.
     * @return The YamlPlayer object, or null if it cannot be found / loaded
     */
    public YamlPlayer getYamlPlayer(UUID uuid, boolean tryLoad) {
        if (this.players.get(uuid) == null && tryLoad)
            this.loadYamlPlayer(uuid);
        return this.players.get(uuid);
    }
}
