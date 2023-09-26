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

import java.lang.Math;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.scheduler.BukkitRunnable;

public class TipScheduler extends BukkitRunnable {
    private Announcer announcer;
    private List<String> tips;

    public TipScheduler(Announcer announcer, List<String> tips) {
        this.announcer = announcer;
        this.tips = tips;
    }

    public void run() {
        announcer.announce(Component.text("[Tip] ").decorate(TextDecoration.BOLD), getRandomTip());
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
