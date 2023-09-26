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

import java.util.Stack;
import java.util.EmptyStackException;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class Announcer {
    private static Style defaultStyle = Style.style()
            .color(NamedTextColor.WHITE)
            .decorate().build();

    private Stack<AnnouncementTask> tasks;

    private Server server;
    private JavaPlugin plugin;

    public Announcer(Server server, JavaPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
        this.tasks = new Stack<AnnouncementTask>();
    }

    /**
     * Announces `message` prefixed with `prefix` to all online players
     *
     * @param prefix  Component to add before `message`, should indicate the "type"
     *                of announcement, will not be modified.
     * @param message The message to send, style will be overwritten for
     *                consistency.
     */
    public void announce(Component prefix, Component message) {
        // I know this looks dumb but otherwise `message`'s style will be overwritten
        server.broadcast(Component.empty()
                .append(prefix)
                .append(message.style(defaultStyle)));
    }

    /**
     * Announces `message` prefixed with `prefix` in `delay` ticks.
     */
    public void announceLater(Component prefix, Component message, long delay) {
        AnnouncementTask task = new AnnouncementTask(prefix, message);
        task.runTaskLater(this.plugin, delay);
        this.tasks.push(task);
    }

    /**
     * Removes `task` from task stack.
     */
    private void removeTask(AnnouncementTask task) {
        this.tasks.remove(task);
    }

    /**
     * Cancels last scheduled announcement.
     * @return The cancelled announcement, or null if there was no scheduled announcements.
     */
    public AnnouncementTask cancelAnnouncement() {
        AnnouncementTask task = null;
        try {
            task = this.tasks.pop();
            task.cancel();
        } catch (EmptyStackException e) {}
        return task;
    }

    public class AnnouncementTask extends BukkitRunnable {
        private Component prefix;
        private Component message;

        public AnnouncementTask(Component prefix, Component message) {
            this.prefix = prefix;
            this.message = message;
        }

        public void run() {
            announce(prefix, message);
            removeTask(this);
        }

        public Component getPrefix() {
            return this.prefix;
        }

        public Component getMessage() {
            return this.message;
        }
    }
}
