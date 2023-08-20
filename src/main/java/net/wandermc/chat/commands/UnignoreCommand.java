package net.wandermc.chat.commands;

import net.wandermc.chat.config.PlayerManager;
import net.wandermc.chat.config.YamlPlayer;

import java.util.logging.Logger;

import net.kyori.adventure.text.Component;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnignoreCommand implements CommandExecutor {
    private Server server;
    private Logger logger;
    private PlayerManager playerManager;

    public UnignoreCommand(Server server, Logger logger, PlayerManager playerManager) {
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

        if (args.length < 1) {
            sender.sendMessage(Component.text("You must specify a player to stop ignoring."));
            return false;
        } 

        // TODO add ability to unignore players who are offline but have previously joined the server
        Player ignoredPlayer = this.server.getPlayerExact(args[0]);
        if (ignoredPlayer == null) {
            sender.sendMessage(Component.text("Player \""+args[0]+"\" doesn't exist or isn't online."));
            return true;
        }

        Player player = (Player)sender;
        YamlPlayer playerConfig = this.playerManager.getYamlPlayer(((Player)sender).getUniqueId(), true); 
        if (!playerConfig.isIgnoring(ignoredPlayer.getUniqueId())) {
            sender.sendMessage(Component.text("Player \""+args[0]+"\" isn't ignored."));
            return true;
        }

        playerConfig.removeIgnored(ignoredPlayer.getUniqueId());

        if (!playerConfig.save()) {
            // TODO in this case store in memory
            logger.warning("Was unable to save data for player with username \""+player.getName()+"\" and UUID \""+player.getUniqueId()+"\".");
            sender.sendMessage(Component.text("An IO error occurred, please notify an admin."));
            return true;
        }

        sender.sendMessage(Component.text("Player \""+args[0]+"\" is no longer ignored."));

        return true;
    }
}
