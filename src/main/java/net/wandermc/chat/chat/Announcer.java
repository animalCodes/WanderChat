package net.wandermc.chat.chat;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;

import org.bukkit.Server;

public class Announcer {
    private static Style defaultStyle = Style.style()
        .color(NamedTextColor.WHITE)
        .decorate().build();

    private Server server;

    public Announcer(Server server) {
        this.server = server;
    }

    /**
     * Announces `message` prefixed with `prefix` to all online players
     *
     * @param prefix Component to add before `message`, should indicate the "type" of announcement, will not be modified.
     * @param message The message to send, style will be overwritten for consistency.
     */
    public void announce(Component prefix, Component message) {
        // I know this looks dumb but otherwise `message`'s style will be overwritten
        server.broadcast(Component.empty()
            .append(prefix)
            .append(message.style(defaultStyle)));
    }
}
