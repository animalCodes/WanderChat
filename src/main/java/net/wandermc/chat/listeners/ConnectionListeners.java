package net.wandermc.chat.listeners;

import net.wandermc.chat.config.PlayerManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListeners implements Listener {
    private PlayerManager playerManager;
    public ConnectionListeners(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.playerManager.loadYamlPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerManager.unloadYamlPlayer(event.getPlayer().getUniqueId());
    }
}

