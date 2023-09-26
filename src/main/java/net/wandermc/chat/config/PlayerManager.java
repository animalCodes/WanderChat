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

import java.util.HashMap;
import java.util.Collection;
import java.util.UUID;

public class PlayerManager {
    private HashMap<UUID, YamlPlayer> players;

    public PlayerManager() {
        this.players = new HashMap<>();
    }

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

    /**
     * Gets all currently stored YamlPlayers.
     *
     * @return A view of all stored YamlPlayers
     */
    public Collection<YamlPlayer> getAllYamlPlayers() {
        return this.players.values();
    }
}
