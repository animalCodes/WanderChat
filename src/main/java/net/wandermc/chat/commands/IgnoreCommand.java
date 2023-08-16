package net.wandermc.chat.commands;

import net.wandermc.chat.config.YamlPlayer;

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

    public IgnoreCommand(Server server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can run this command."));
            return true;
        } 
        if (args.length < 1) {
            sender.sendMessage(Component.text("You must specify a player to ignore."));
            return false;
        } 

        Player ignoredPlayer = this.server.getPlayerExact(args[0]);
        if (ignoredPlayer == null) {
            sender.sendMessage(Component.text("Player \""+args[0]+"\" doesn't exist or isn't online."));
            return true;
        }

        Player player = (Player)sender;
        YamlPlayer playerConfig = new YamlPlayer(player.getUniqueId()); 
        if (player == ignoredPlayer) {
            sender.sendMessage(Component.text("You can't ignore yourself."));
            return true;
        }

        playerConfig.appendIgnored(ignoredPlayer.getUniqueId());

        logger.info(playerConfig.getIgnored().toString());

        if (!playerConfig.save()) {
            // TODO in this case store in memory
            logger.warning("Was unable to save data for player with username \""+player.name()+"\" and UUID \""+player.getUniqueId()+"\".");
            sender.sendMessage(Component.text("An IO error occurred, please notify an admin."));
        }

        return true;
    }
}
