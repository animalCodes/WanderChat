package net.wandermc.chat.chat;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.TextComponent;

public class Formatters {
    // Shorthand styles
    private static final Style underlineStyle = Style.style().decorate(TextDecoration.UNDERLINED).build();

    // Should match *most* URLs + leading text if it exists
    // Full url stored in "url" group, leading text is similarly stored in "leading" group.
    private static final Pattern urlRegex = Pattern.compile("(?<leading>.*?)(?<url>(?:https?://)?(?:[\\w-]+\\.)+(?:[\\w-]+)(?:/?[\\w-.~!?$&'()*+,;=:@%]*/?)*(?:\\.\\w+)?(?:#[\\w-.~!?$&'()*+,;=:@%]*)?)");

    // Attempts to locate any urls (matching the pattern found in `urlRegex`) in the content of `component` and returns a new Component with those urls made clickable.
    public static final Function<TextComponent, TextComponent> makeLinksClickable = component -> {
        TextComponent.Builder newMessage = Component.text();
        Matcher urlMatcher = urlRegex.matcher(component.content());

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
                    .mergeStyle(component)
                    // Minecraft only considers links valid if they have a protocol specified, so add one if needed.
                    .clickEvent(ClickEvent.openUrl(!url.startsWith("http") ? "https://" + url : url))); 

            endIndex = urlMatcher.end();
        }

        // Append any trailing text. If no matches were found, this'll append the entire content of `textComponent`.
        newMessage.append(Component.text(component.content().substring(endIndex, component.content().length()))
                .mergeStyle(component));
        return newMessage.build();
    };
}
