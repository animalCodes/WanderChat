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

import net.wandermc.chat.config.PlayerManager;
import net.wandermc.chat.config.YamlPlayer;

import net.kyori.adventure.text.Component;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WcaCommand implements CommandExecutor {
    private PlayerManager playerManager;

    // All subcommands with descriptions.
    private String[] subCommandDescs = {
            "help - list valid subcommands",
            "updateplayercache - force update of in-memory player cache, to be used after modifying a player yaml file."
    };

    public WcaCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("No subcommand specified, try /wca help for a list."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            // Ensure all YamlPlayers currently held by PlayerManager are storing the values shown in their files.
            case "updateplayercache": {
                sender.sendMessage(Component.text("Updating cache.."));
                for (YamlPlayer yamlPlayer : playerManager.getAllYamlPlayers())
                    yamlPlayer.reloadFile();

                sender.sendMessage(Component.text("Done!"));
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
}
