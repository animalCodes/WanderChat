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

import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.Set;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.chat.chat.ChatMessage;
import net.wandermc.chat.config.PlayerManager;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private static final Style boldStyle = Style.style().decorate(TextDecoration.BOLD).build();
    private static final Style italicStyle = Style.style().decorate(TextDecoration.ITALIC).build();

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
        ChatMessage chatMessage = new ChatMessage((TextComponent)event.message());

        chatMessage.detectUrls();
        chatMessage.styleMarkedText(boldStyle, "**");
        chatMessage.styleMarkedText(italicStyle, "*");
        
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

        event.message(chatMessage.getMessage());
    }
}
