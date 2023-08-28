package net.wandermc.chat.chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ChatMessage {
    // Shorthand for a style that just adds an underline, 'tis a bit complicated
    private static Style underlineStyle = Style.style().decorate(TextDecoration.UNDERLINED).build();
    // Should match all valid username characters
    // Actual username stored in "username" group
    private static final Pattern taggedUsernameRegex = Pattern.compile("@(?<username>\\w+)\\W?");
    // Should match *most* URLs + leading text if it exists
    // Full url stored in "url" group, leading text is similarly stored in "leading" group.
    private static final Pattern urlRegex = Pattern.compile("(?<leading>.*?)(?<url>(?:https?://)?(?:[\\w-]+\\.)+(?:[\\w-]+)(?:/?[\\w-.~!?$&'()*+,;=:@%]*/?)*(?:\\.\\w+)?(?:#[\\w-.~!?$&'()*+,;=:@%]*)?)"); 

    private Component originalMessage;
    private TextComponent message;

    public ChatMessage(Component message) {
        this.originalMessage = message;
        this.message = makeLinksClickable(message);
    }

    /**
     * Attempts to locate any urls (matching the pattern found in `urlRegex`) in `message` and returns a new Component with those urls made clickable.
     *
     * @param message The message to parse
     * @return The parsed message
     */
    private static TextComponent makeLinksClickable(Component message) {
        TextComponent.Builder newMessage = Component.text();
        
        // Iterate over message and its children, their children, their children..
        message.iterator(ComponentIteratorType.DEPTH_FIRST, EnumSet.noneOf(ComponentIteratorFlag.class))
            .forEachRemaining(component -> {
                // Only look for urls in TextComponents
                if (component instanceof TextComponent textComponent) {
                    Matcher urlMatcher = urlRegex.matcher(textComponent.content());

                    int endIndex = 0;

                    // For every match
                    while (urlMatcher.find()) {
                        // Append text before match (this may be an empty string)
                        newMessage.append(Component.text(urlMatcher.group("leading"))
                                .mergeStyle(component)); // Retain styling

                        String url = urlMatcher.group("url");
                        // Minecraft only considers links valid if they have a protocol specified, so add one if needed.
                        String withProtocol = !url.startsWith("http") ? "https://"+url : url;
                        // Append found url, make clickable.
                        newMessage.append(Component.text(url)
                                .style(underlineStyle) // Add underline as a marker that the link has been made clickable
                                .mergeStyle(component)
                                .clickEvent(ClickEvent.openUrl(withProtocol))); // Adding the styling seems to wipe the clickEvent, so add last.

                        endIndex = urlMatcher.end();
                    }

                    // Append any trailing text
                    // If no matches were found, this'll append the entire content of `textComponent`.
                    newMessage.append(Component.text(textComponent.content().substring(endIndex, textComponent.content().length())));
                }
            });
        
        return (TextComponent)newMessage.build().compact();
    }

    /**
     * Returns a set of all players tagged in this message.
     *
     * @param server Current Server instance, used for converting usernames to
     *               Players.
     * @return A set of players whose usernames were tagged in this message
     */
    public HashSet<Player> getTaggedPlayers(Server server) {
        ArrayList<String> usernames = new ArrayList<>();
        HashSet<Player> taggedPlayers = new HashSet<>();

        Matcher usernameMatcher = taggedUsernameRegex.matcher(
                PlainTextComponentSerializer.plainText().serialize(this.originalMessage));

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
    public Component getOriginalMessage() {
        return this.originalMessage;
    }

    /**
     * Gets message, this will likely have been modified.
     *
     * To access the original message used to create this instance, use `ChatMessage.getOriginalMessage()`
     * 
     * @return The message that should be sent to other players
     */
    public TextComponent getMessage() {
        return this.message;
    }
}
