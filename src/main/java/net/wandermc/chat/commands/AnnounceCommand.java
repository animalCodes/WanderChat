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
package net.wandermc.chat.commands;

import net.wandermc.chat.chat.Announcer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnnounceCommand implements CommandExecutor {
    // Pattern to match first argument given to "in" subcommand. (Duration)
    private final Pattern durationPattern = Pattern.compile("((?<hours>\\d+)[hH])?((?<minutes>\\d+)[mM])?((?<seconds>\\d+)[sS])?");

    private Announcer announcer;

    // All subcommands with descriptions.
    private String[] subCommandDescs = {
            "in {duration} {message} - Announce {message} after {duration}, run without specifying a duration or message to see duration format.",
            "cancel - Cancels the last scheduled announcement.",
            "help - List valid subcommands."
    };

    public AnnounceCommand(Announcer announcer) {
        this.announcer = announcer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("No subcommand specified, try \"/announce\" help for a list."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            // Announce {message} after {duration}
            case "in": {
                if (!announceIn(args, sender))
                    sender.sendMessage(Component.text("Usage: "+this.subCommandDescs[0]));
                break;
            }
            case "cancel": {
                announceCancel(sender);
                break;
            }
            // List all subcommands with descriptions.
            case "help":
            default: {
                sender.sendMessage(Component.text("Valid subcommands:"));
                for (String message : this.subCommandDescs)
                    sender.sendMessage(Component.text(message));
                break;
            }
        }

        return true;
    }

    /**
     * Handles "in" subcommand
     * 
     * @param args All arguments the /announce command was run with
     * @param sender The caller of the command
     * @return Whether the command ran successfully.
     */
    private boolean announceIn(String[] args, CommandSender sender) {
        // If no extra arguments are given, print duration format.
        if (args.length < 2) {
            sender.sendMessage(Component.text("Duration format: [{n}h][{n}m][{n}s] where"));
            sender.sendMessage(Component.text(" {n} Represents any whole number, this can be 0, but shouldn't be."));
            sender.sendMessage(Component.text(" [] Indicates optionality: each number-pair is optional, but at least one must be specified."));
            sender.sendMessage(Component.text(" h, m and s stand for \"Hours\", \"Minutes\" and \"Seconds\" respectively, and are case-insensitive."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Please specify a message to announce."));
            return false;
        }
        Matcher matcher = durationPattern.matcher(args[1]);
        // If the duration format is invalid, rerun with empty args to force printing of duration format.
        if (!matcher.matches()) {
            return announceIn(new String[0], sender);
        } else {
            // Matcher.group() will return null if the group didn't match anything, and Integer.parseInt() will throw a NumberFormatException if given a null value. Ternary operators to the rescue!
            int hours = Integer.parseInt(matcher.group("hours") == null ? "0" : matcher.group("hours"));
            int minutes = Integer.parseInt(matcher.group("minutes") == null ? "0" : matcher.group("minutes"));
            int seconds = Integer.parseInt(matcher.group("seconds") == null ? "0" : matcher.group("seconds"));

            long ticks = (hours * 60 * 60 * 20) + (minutes * 60 * 20) + (seconds * 20);

            // Grab rest of arguments as message
            StringBuffer builder = new StringBuffer();
            builder.append(args[2]);
            for (int i = 3; i < args.length; i++)
                builder.append(" "+args[i]);
            String message = builder.toString();
            
            sender.sendMessage(Component.text(String.format("Announcing \"%s\" in %d hours, %d minutes and %d seconds. (%d ticks)", message, hours, minutes, seconds, ticks)));
            sender.sendMessage(Component.text("Use /announce cancel to undo."));

            this.announcer.announceLater(Component.text("[Announcement] ").decorate(TextDecoration.BOLD), Component.text(message), ticks);
        }
        return true;
    }

    /**
     * Handle "cancel" subcommand.
     *
     * @param sender The caller of the command
     * @return Whether an announcement was cancelled (there might not be any scheduled.)
     */
    private boolean announceCancel(CommandSender sender) {
        Announcer.AnnouncementTask task = this.announcer.cancelAnnouncement();
        if (task == null) {
            sender.sendMessage(Component.text("There isn't any pending announcements to cancel."));
            return false;
        }
        sender.sendMessage(Component.text("Announcement \"").append(task.getMessage()).append(Component.text("\" cancelled.")));
        return true;
    }
}
