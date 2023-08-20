package net.wandermc.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.Set;

import net.kyori.adventure.audience.Audience;

import net.wandermc.chat.config.PlayerManager;
import net.wandermc.chat.config.YamlPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private PlayerManager playerManager;

    public ChatListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    @EventHandler
    public void onMessage(AsyncChatEvent event) {
        Set<Audience> recipients = event.viewers();
        // If a player recipient is ignoring the sender, remove them from the recipient list.
        recipients.removeIf(recipient -> {
            if (recipient instanceof Player playerReceiver) {
                return (this.playerManager.getYamlPlayer(playerReceiver.getUniqueId(), false).isIgnoring(event.getPlayer().getUniqueId()));
            }
            return false;
        });
    }
}
