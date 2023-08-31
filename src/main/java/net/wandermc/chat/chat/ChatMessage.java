package net.wandermc.chat.chat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ChatMessage {
    // Should match all valid username characters
    // Actual username stored in "username" group
    private static final Pattern taggedUsernameRegex = Pattern.compile("@(?<username>\\w+)\\W?");

    private TextComponent originalMessage;
    private TextComponent message;

    public ChatMessage(TextComponent message) {
        this.originalMessage = message;
        this.message = this.originalMessage;
    }

    /**
     * Applies given formatting function to every component comprising this message.
     *
     * A "formatting function" should take a TextComponent as input, modify it in some way (it doesn't actually need to change the "format" of the component) and return the modified version.
     *
     * @param function The function to apply
     */
    public void applyFormatter(Function<TextComponent, TextComponent> function) {
        TextComponent.Builder newMessagebuilder = Component.text();
        this.message.iterator(ComponentIteratorType.DEPTH_FIRST, EnumSet.noneOf(ComponentIteratorFlag.class))
            .forEachRemaining(component -> {
                if (component instanceof TextComponent textComponent) {
                    newMessagebuilder.append(function.apply(textComponent));
                }
            });
        this.message = (TextComponent)newMessagebuilder.build().compact();
    }

    /**
     * Returns a set of all players tagged in this message.
     *
     * @param server Current Server instance, used for converting usernames to Players.
     * @return A set of players whose usernames were tagged in this message
     */
    public HashSet<Player> getTaggedPlayers(Server server) {
        ArrayList<String> usernames = new ArrayList<>();
        HashSet<Player> taggedPlayers = new HashSet<>();

        Matcher usernameMatcher = taggedUsernameRegex.matcher(PlainTextComponentSerializer.plainText().serialize(this.originalMessage));

        while (usernameMatcher.find()) {
            usernames.add(usernameMatcher.group("username"));
        }

        usernames.forEach(username -> {
            // TODO make configurable whether to use getPlayerExact or getPlayer
            Player taggedPlayer = server.getPlayerExact(username);
            if (taggedPlayer != null)
                taggedPlayers.add(taggedPlayer);
        });

        return taggedPlayers;
    }

    /**
     * Gets the original, unmodified Component used to create this instance.
     *
     * @return The original message
     */
    public TextComponent getOriginalMessage() {
        return this.originalMessage;
    }

    /**
     * Gets message, this may differ from the original component used to create this class.
     *
     * To access the original message used to create this instance, use
     * `ChatMessage.getOriginalMessage()`
     * 
     * @return The message that should be sent to other players
     */
    public TextComponent getMessage() {
        return this.message;
    }
}
