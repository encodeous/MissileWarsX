package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import lobbyengine.Lobby;
import lobbyengine.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwstartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Lobby lobby = null;
            if(sender instanceof Player){
                if(LobbyEngine.FromPlayer((Player) sender) != null){
                    lobby = LobbyEngine.FromPlayer((Player) sender).lobby;
                }
            }
            if(lobby == null){
                lobby = LobbyEngine.GetLobby("default");
            }
            MissileWarsMatch match = lobby.Match;
            if(match.isStarting || match.isCleaning){
                sender.sendMessage("The game cannot be started at this time!");
            }else if(!match.hasStarted){
                match.isStarting = true;
                match.startCounter.Start();
            }else{
                sender.sendMessage("Game already started!");
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
