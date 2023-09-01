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
    private static final Style boldStyle = Style.style().decorate(TextDecoration.BOLD).build();
    private static final Style italicStyle = Style.style().decorate(TextDecoration.ITALIC).build();

    // Should match *most* URLs + leading text if it exists
    // Full url stored in "url" group, leading text is similarly stored in "leading" group.
    private static final Pattern urlRegex = Pattern.compile("(?<leading>.*?)(?<url>(?:https?://)?(?:[\\w-]+\\.)+(?:[\\w-]+)(?:/?[\\w-.~!?$&'()*+,;=:@%]*/?)*(?:\\.\\w+)?(?:#[\\w-.~!?$&'()*+,;=:@%]*)?)");
    // Should match at least a single character surrounded by exactly "**" on either side + leading text.
    // As above, the marked text is stored in the "text" group, and the leading text is stored in the "leading" group.
    private static final Pattern strongTextRegex = Pattern.compile("(?<leading>.*?)\\*\\*(?<text>.+)\\*\\*");
    // Literally identical to above but with one '*' instead of two.
    private static final Pattern emphasisedTextRegex = Pattern.compile("(?<leading>.*?)\\*(?<text>.+)\\*");

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

    // Makes any text surrounded by "**" on both sides bold.
    public static final Function<TextComponent, TextComponent> makeStrongTextBold = component -> {
        TextComponent.Builder newMessage = Component.text();
        Matcher urlMatcher = strongTextRegex.matcher(component.content());

        int endIndex = 0;
        while (urlMatcher.find()) {
            newMessage.append(Component.text(urlMatcher.group("leading"))
                    .mergeStyle(component));

            newMessage.append(Component.text(urlMatcher.group("text"))
                    .style(boldStyle)
                    .mergeStyle(component)); 

            endIndex = urlMatcher.end();
        }
        newMessage.append(Component.text(component.content().substring(endIndex, component.content().length()))
                .mergeStyle(component));
        return newMessage.build();
    };

    // Makes text surrounded by a single '*' on either side italic.
    public static final Function<TextComponent, TextComponent> makeEmphasisedTextItalic = component -> {
        TextComponent.Builder newMessage = Component.text();
        Matcher urlMatcher = emphasisedTextRegex.matcher(component.content());

        int endIndex = 0;
        while (urlMatcher.find()) {
            newMessage.append(Component.text(urlMatcher.group("leading"))
                    .mergeStyle(component));

            newMessage.append(Component.text(urlMatcher.group("text"))
                    .style(italicStyle)
                    .mergeStyle(component)); 

            endIndex = urlMatcher.end();
        }
        newMessage.append(Component.text(component.content().substring(endIndex, component.content().length()))
                .mergeStyle(component));
        return newMessage.build();
    };
}
