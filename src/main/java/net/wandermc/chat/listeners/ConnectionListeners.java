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
package net.wandermc.chat.listeners;

import net.wandermc.chat.config.PlayerManager;
import net.wandermc.chat.chat.Announcer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListeners implements Listener {
    private PlayerManager playerManager;
    private Announcer announcer;
    public ConnectionListeners(Announcer announcer, PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.announcer = announcer;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.playerManager.loadYamlPlayer(event.getPlayer().getUniqueId());

        // Restart TipScheduler if it was previously stopped (due to no players being online)
        if (this.announcer.getTipScheduler().isCancelled()) {
            announcer.log("Restarting tip schedule");
            this.announcer.startTipScheduler();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerManager.unloadYamlPlayer(event.getPlayer().getUniqueId());
    }
}

