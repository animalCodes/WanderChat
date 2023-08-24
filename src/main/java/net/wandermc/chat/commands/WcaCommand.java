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
