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

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class Announcer {
    private static Style defaultStyle = Style.style()
            .color(NamedTextColor.WHITE)
            .decorate().build();

    private TipScheduler tipScheduler;
    private Stack<AnnouncementTask> tasks;

    private Server server;
    private JavaPlugin plugin;

    private List<String> tipsList;
    private int tipsDelay = 0;

    public Announcer(Server server, JavaPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
        this.tasks = new Stack<AnnouncementTask>();
    }

    /**
     * Logs `message` to console.
     *
     * @param message The message to send.
     */
    public void log(String message) {
        // TODO clean this up
        this.plugin.getLogger().info(message);
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

    // For announcing `prefix` and `message` after a set period
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

    /**
     * Starts a TipScheduler.
     *
     * Can also be used to update the tips list and delay used for future TipSchedulers.
     * 
     * @param tips The list of tips to choose from.
     * @param delay The delay in minutes to pause between each tip announcement.
     */
    public void startTipScheduler(List<String> tips, int delay) {
        this.tipsList = tips;
        this.tipsDelay = delay;

        this.startTipScheduler();
    }

    /**
     * Attempts to start a TipScheduler, using the tip list and delay previously given to this Announcer.
     *
     * Announcer.startTipScheduler(tips, delay) MUST be run before this.
     *
     * @return Whether a TipScheduler was successfully started.
     */
    public boolean startTipScheduler() {
        if (this.tipsDelay != 0 && this.tipsList != null) {
            this.log("Starting tip schedule");
            this.tipScheduler = new TipScheduler(this.tipsList);
            this.tipScheduler.runTaskTimer(this.plugin, 20*60*5, this.tipsDelay);
            return true;
        }
        return false;
    }

    /**
     * Gets the current TipScheduler
     *
     * @return Current TipScheduler
     */
    public TipScheduler getTipScheduler() {
        return this.tipScheduler;
    }

    // For sending a random tip from `tips` every few minutes
    public class TipScheduler extends BukkitRunnable {
        private List<String> tips;

        private TipScheduler(List<String> tips) {
            this.tips = tips;
        }

        public void run() {
            if (server.getOnlinePlayers().isEmpty()) {
                plugin.getLogger().info("Server empty, stopping tip schedule");
                this.cancel();
                return;
            }
            announce(Component.text("[Tip] ").decorate(TextDecoration.BOLD), getRandomTip());
        }

        /**
         * Gets a random tip from `this.tips` as a TextComponent
         *
         * @return A random tip
         */
        private TextComponent getRandomTip() {
            return Component.text(this.tips.get((int) (Math.random() * this.tips.size())));
        }
    }
}
