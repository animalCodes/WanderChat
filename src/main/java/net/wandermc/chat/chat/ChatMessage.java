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
package net.wandermc.chat.chat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ChatMessage {
    private static final Style underlineStyle = Style.style().decorate(TextDecoration.UNDERLINED).build();
    // Should match all valid username characters
    // Actual username stored in "username" group
    private static final Pattern taggedUsernameRegex = Pattern.compile("@(?<username>\\w+)\\W?");
    // Should match *most* URLs + leading text if it exists
    // Full url stored in "url" group, leading text is similarly stored in "leading"
    // group.
    private static final Pattern urlRegex = Pattern.compile(
            "(?<leading>.*?)(?<url>(?:https?://)?(?:[\\w-]+\\.)+(?:[\\w-]+)(?:/?[\\w-.~!?$&'()*+,;=:@%]*/?)*(?:\\.\\w+)?(?:#[\\w-.~!?$&'()*+,;=:@%]*)?)");

    private TextComponent originalMessage;
    private TextComponent message;

    public ChatMessage(TextComponent message) {
        this.originalMessage = message;
        this.message = this.originalMessage;
    }

    /**
     * Attempts to locate every url in this message and make them clickable.
     *
     * Due to this operating on single components and thus being unable to detect urls that span multiple components, this should be run before any calls to `styleMarkedText()`.
     */
    public void detectUrls() {
        TextComponent.Builder newMessage = Component.text();
        this.message.iterator(ComponentIteratorType.DEPTH_FIRST, EnumSet.noneOf(ComponentIteratorFlag.class))
                .forEachRemaining(component -> {
                    if (component instanceof TextComponent textComponent) {
                        Matcher urlMatcher = urlRegex.matcher(textComponent.content());
                        int endIndex = 0;
                        // For every match
                        while (urlMatcher.find()) {
                            // Append text before matching URL (this may be an empty string)
                            newMessage.append(Component.text(urlMatcher.group("leading"))
                                    .mergeStyle(component)); // Retain styling

                            // Append found url, make clickable.
                            String url = urlMatcher.group("url");
                            newMessage.append(Component.text(url)
                                    .style(underlineStyle) // Add underline as a marker that the link has been made clickable
                                    .mergeStyle(textComponent)
                                    // Minecraft only considers links valid if they have a protocol specified, so
                                    // add one if needed.
                                    .clickEvent(ClickEvent.openUrl(!url.startsWith("http") ? "https://" + url : url)));

                            endIndex = urlMatcher.end();
                        }

                        // Append any trailing text. If no matches were found, this'll append the entire
                        // content of `textComponent`.
                        newMessage.append(Component
                                .text(textComponent.content().substring(endIndex, textComponent.content().length()))
                                .mergeStyle(textComponent));
                    }
                });
        this.message = (TextComponent) newMessage.build().compact();
    }

    /**
     * Locates text surrounded by `mark` on either side, extracts and applies
     * `style` to it.
     * 
     * Any previous style will be preserved.
     *
     * @param style The style to apply.
     * @param mark  The substrings indicating text to be styled.
     */
    public void styleMarkedText(Style style, String mark) {
        TextComponent.Builder newMessage = Component.text();
        TextComponent.Builder buffer = Component.text();

        int startIndex = 0;
        int endIndex = 0;

        Iterator<Component> iterator = this.message.iterator(ComponentIteratorType.DEPTH_FIRST,
                EnumSet.noneOf(ComponentIteratorFlag.class));
        while (iterator.hasNext()) {
            TextComponent component = (TextComponent) iterator.next();
            String content = component.content();

            // `endIndex != -1` can be read as "another mark was found"
            do {
                // Locate UNMARKED text
                // Find next mark after startIndex
                endIndex = content.indexOf(mark, startIndex);
                // Append all text before mark, retain styling.
                newMessage.append(
                        Component.text(content.substring(startIndex, (endIndex == -1) ? content.length() : endIndex))
                                .mergeStyle(component));
                // If a mark was found, locate MARKED text.
                if (endIndex != -1) {
                    startIndex = endIndex + mark.length(); // Step over found mark
                    endIndex = content.indexOf(mark, startIndex); // Find next mark if it exists
                    if (endIndex == -1) { // No marks after first
                        // Append trailing text
                        buffer.append(Component.text(content.substring(startIndex, content.length()))
                                .mergeStyle(component));
                        // Try to find mark in subsequent components
                        boolean found = false;
                        while (iterator.hasNext()) {
                            component = (TextComponent) iterator.next();
                            content = component.content();
                            endIndex = content.indexOf(mark);
                            if (endIndex != -1) {
                                found = true;
                                buffer.append(Component.text(content.substring(0, endIndex))
                                        .mergeStyle(component));
                                break;
                            } else {
                                buffer.append(component);
                            }
                        }
                        // If another mark was found, style all inbetween components
                        if (found) {
                            // This is ugly and overcomplicated but also the only way I could figure out to
                            // preserve the previous style of the component.
                            for (Component child : buffer.children()) {
                                newMessage.append(child.style(style).mergeStyle(child));
                            }
                        } else { // Otherwise re-add mark and leave styling as-is
                            newMessage.append(Component.text(mark));
                            newMessage.append(buffer.build());
                        }
                        buffer = Component.text();
                    } else { // Next mark was found
                        // Deal with case where two marks are placed directly next to each other
                        // This won't help if the marks are at the end+start of adjacent components, but
                        // it's enough for now.
                        if (startIndex == endIndex)
                            newMessage.append(Component.text(mark + mark)
                                    .mergeStyle(component));
                        else
                            newMessage.append(Component.text(content.substring(startIndex, endIndex))
                                    .style(style)
                                    .mergeStyle(component));
                    }
                    startIndex = (endIndex + mark.length() > content.length()) ? content.length() : endIndex + mark.length(); // Again step over mark in preparation for next loop
                }
            } while (endIndex != -1);
            startIndex = 0;
            endIndex = 0;
        }
        this.message = (TextComponent) newMessage.build().compact();
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

        Matcher usernameMatcher = taggedUsernameRegex
                .matcher(PlainTextComponentSerializer.plainText().serialize(this.originalMessage));

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
     * Gets message, this may differ from the original component used to create this
     * class.
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
