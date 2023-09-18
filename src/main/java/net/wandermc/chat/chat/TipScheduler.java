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
