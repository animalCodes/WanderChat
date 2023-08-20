package net.wandermc.chat.commands;

import net.wandermc.chat.config.PlayerManager;
import net.wandermc.chat.config.YamlPlayer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand implements CommandExecutor {
    private Server server;
    private Logger logger;
    private PlayerManager playerManager;

    public IgnoreCommand(Server server, Logger logger, PlayerManager playerManager) {
        this.server = server;
        this.logger = logger;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can run this command."));
            return true;
        }

        Player player = (Player) sender;
        YamlPlayer playerConfig = this.playerManager.getYamlPlayer(player.getUniqueId(), true);

        // If run without arguments, list currently ignored players
        if (args.length < 1) {
            ArrayList<UUID> ignoredPlayers = (ArrayList<UUID>) playerConfig.getIgnored();
            if (ignoredPlayers.isEmpty()) {
                sender.sendMessage(Component.text("You aren't ignoring any players."));
                // Prompt with correct usage in case the caller forgot to specify a username
                return false;
            } else {
                sender.sendMessage(Component.text("Currently ignored players:"));
                ignoredPlayers.forEach(uuid -> {
                    sender.sendMessage(Component.text(" - " + this.server.getOfflinePlayer(uuid).getName()));
                });
                return true;
            }
        }

        Player ignoredPlayer = this.server.getPlayerExact(args[0]);
        if (ignoredPlayer == null) {
            sender.sendMessage(Component.text("Player \"" + args[0] + "\" doesn't exist or isn't online."));
            return true;
        }

        if (player == ignoredPlayer) {
            sender.sendMessage(Component.text("You can't ignore yourself."));
            return true;
        }

        if (playerConfig.isIgnoring(ignoredPlayer.getUniqueId())) {
            sender.sendMessage(Component.text("Player \"" + args[0] + "\" is already ignored, to un-ignore someone use /unignore."));
            return true;
        }

        playerConfig.appendIgnored(ignoredPlayer.getUniqueId());

        if (!playerConfig.save()) {
            // TODO in this case store in memory
            logger.warning("Was unable to save data for player with username \"" + player.getName() + "\" and UUID \"" + player.getUniqueId() + "\".");
            sender.sendMessage(Component.text("An IO error occurred, please notify an admin."));
            return true;
        }

        sender.sendMessage(Component.text("You will no longer see messages sent by player \"" + args[0] + "\", to undo use /unignore."));

        return true;
    }
}
