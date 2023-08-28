package net.wandermc.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import net.wandermc.chat.chat.ChatMessage;
import net.wandermc.chat.config.PlayerManager;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private Server server;
    private PlayerManager playerManager;

    public ChatListener(Server server, PlayerManager playerManager) {
        this.server = server;
        this.playerManager = playerManager;
    }

    /**
     * Figures out whether `tagger` should be allowed to tag `player`.
     *
     * Checks if `player` is ignoring `tagger`
     *
     * @param tagger The player doing the tagging
     * @param player The player being tagged
     * @return Whether `tagger` can tag `player`
     */
    private boolean canTag(Player tagger, Player player) {
            return !this.playerManager.getYamlPlayer(player.getUniqueId(), true).isIgnoring(tagger.getUniqueId());
    }

    /**
     * "Tags" player `taggee`, playing a sound and sending them an actionbar message.
     *
     * @param taggee The player being tagged
     * @param taggerName The displayName of the player doing the tagging
     */
    private void tagPlayer(Player taggee, Component taggerName) {
        taggee.playSound(taggee.getLocation(), Sound.BLOCK_LARGE_AMETHYST_BUD_BREAK, 5, 2);

        taggee.sendActionBar(taggerName
                .append(Component.text(" Just tagged you in chat.")
                .color(NamedTextColor.WHITE)));
    }

    @EventHandler
    public void onMessage(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        ChatMessage chatMessage = new ChatMessage(event.message());

        // Tag any tagged players provided sender is allowed to tag them.
        for (Player player : chatMessage.getTaggedPlayers(this.server)) {
            if (canTag(sender, player))
                tagPlayer(player, sender.displayName());
        }

        // If a player recipient is ignoring the sender, remove them from the recipient list.
        Set<Audience> recipients = event.viewers();
        recipients.removeIf(recipient -> {
            if (recipient instanceof Player playerReceiver) {
                try {
                    return (this.playerManager.getYamlPlayer(playerReceiver.getUniqueId(), false)
                            .isIgnoring(event.getPlayer().getUniqueId()));
                    // Will occur if YamlPlayer isn't loaded
                } catch (NullPointerException e) {
                    return false;
                }
            }
            return false;
        });

        // ChatMessage has likely made some changes to the original message, so update the message to be sent.
        event.message(chatMessage.getMessage());
    }
}
