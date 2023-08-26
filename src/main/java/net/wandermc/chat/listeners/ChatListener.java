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

import net.wandermc.chat.config.PlayerManager;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    // Should match all valid username characters
    // Actual username stored in "username" group
    private static final Pattern taggedUsernameRegex = Pattern.compile("@(?<username>\\w+)\\W?");

    private Server server;
    private PlayerManager playerManager;

    public ChatListener(Server server, PlayerManager playerManager) {
        this.server = server;
        this.playerManager = playerManager;
    }

    /**
     * Returns a set of all players tagged in `chatMessage`.
     *
     * @param chatMessage Message to scan through
     * @return A set of players whose usernames were tagged in the message
     */
    private HashSet<Player> getTaggedPlayers(Component chatMessage) {
        ArrayList<String> usernames = new ArrayList<>();
        HashSet<Player> taggedPlayers = new HashSet<>();

        Matcher usernameMatcher = taggedUsernameRegex.matcher(
                PlainTextComponentSerializer.plainText().serialize(chatMessage));

        while (usernameMatcher.find()) {
            usernames.add(usernameMatcher.group("username"));
        }

        usernames.forEach(username -> {
            // TODO make configurable whether to use getPlayerExact or getPlayer
            Player taggedPlayer = this.server.getPlayerExact(username);
            if (taggedPlayer != null)
                taggedPlayers.add(taggedPlayer);
        });

        return taggedPlayers;
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

        // Tag any tagged players provided sender is allowed to tag them.
        for (Player player : getTaggedPlayers(event.message())) {
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
    }
}
