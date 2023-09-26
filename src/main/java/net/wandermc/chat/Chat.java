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
package net.wandermc.chat;

import net.wandermc.chat.chat.Announcer;
import net.wandermc.chat.chat.TipScheduler;
import net.wandermc.chat.commands.*;
import net.wandermc.chat.config.*;
import net.wandermc.chat.listeners.*;

import java.io.File;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
    private File usersFolder;

    private PlayerManager playerManager;
    private Announcer announcer;

    private TipScheduler tipScheduler;

    public void onEnable() {
        setupUsersFolder();

        // I don't like constantly calling `this.getServer()`.
        Server server = this.getServer();

        this.playerManager = new PlayerManager();
        this.announcer = new Announcer(server, this);

        getCommand("ignore").setExecutor(new IgnoreCommand(server, this.getLogger(), this.playerManager));
        getCommand("unignore").setExecutor(new UnignoreCommand(server, this.getLogger(), this.playerManager));
        getCommand("wca").setExecutor(new WcaCommand(this.playerManager));
        getCommand("announce").setExecutor(new AnnounceCommand(this.announcer));

        getServer().getPluginManager().registerEvents(new ChatListener(server, this.playerManager), this);
        getServer().getPluginManager().registerEvents(new ConnectionListeners(this.playerManager), this);

        List<String> tips = getTips();
        if (tips.isEmpty()) {
            this.getLogger().warning("No tips in config.yml, so none will be announced.");
        } else {
            this.tipScheduler = new TipScheduler(this.announcer, tips);
            // 20 ticks in a second * 60 seconds in a minute * x minutes
            this.tipScheduler.runTaskTimer(this, 20*60*5,
                    20 * 60 * this.getConfig().getInt("tipDelay", 15));
        }
    }

    public void onDisable() {}

    /**
     * Ensures existence of "users" config directory and calls setUsersFolder for YamlPlayer.
     */
    private void setupUsersFolder() {
        // Ensure existence of usersFolder
        this.usersFolder = new File(this.getDataFolder(), "users");
        this.usersFolder.mkdir();
        // Removing this line will cause the universe to collapse
        YamlPlayer.setUsersFolder(this.usersFolder);
    }

    /**
     * Retrieves all tips from config.yml
     * 
     * @return All tips to yeet at online players
     */
    private List<String> getTips() {
        return this.getConfig().getStringList("tips");
    }
}
