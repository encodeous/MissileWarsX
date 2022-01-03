package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class lobbyCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(args.length == 0){
                    MissileWarsMatch match = LobbyEngine.FromPlayer((Player) sender);
                    match.RemovePlayer((Player) sender);
                    match.AddPlayerToTeam((Player) sender, PlayerTeam.None);
                }else if(args.length == 1){
                    try{
                        int val = Integer.parseInt(args[0]);
                        if(val <= 0 || val > LobbyEngine.Lobbies.size()){
                            sender.sendMessage("The lobby you specified does not exist.");
                        }else{
                            LobbyEngine.FromPlayer((Player) sender).lobby.RemovePlayer((Player) sender);
                            LobbyEngine.GetLobby(val).AddPlayer((Player) sender);
                            sender.sendMessage(Chat.FCL("&9You have been teleported to lobby " + val + "."));
                        }
                    }catch (NumberFormatException e){
                        return false;
                    }
                }else return false;

            }
            else{
                sender.sendMessage("You are not a player...");
            }
            return true;
        }catch (Exception e){

        }
        return false;
    }

    @Override
    public RootCommand BuildCommand() {
        throw new NotImplementedException("Building this command is not implemented");
    }

    @Override
    public String GetCommandName() {
        return "lobby";
    }
}
