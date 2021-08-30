package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import lobbyengine.Lobby;
import lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwteamCommand implements CommandExecutor {
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
                lobby = LobbyEngine.GetLobby(0);
            }
            MissileWarsMatch match = lobby.Match;
            Player p = Bukkit.getPlayer(args[0]);
            if(p == null){
                sender.sendMessage("Player not found!");
                return true;
            }
            if(args[1].equals("green")){
                match.AddPlayerToTeam(p, PlayerTeam.Green);
            } else if(args[1].equals("red")){
                match.AddPlayerToTeam(p, PlayerTeam.Red);
            } else if(args[1].equals("spectator")){
                match.AddPlayerToTeam(p, PlayerTeam.Spectator);
            } else if(args[1].equals("lobby")){
                match.AddPlayerToTeam(p, PlayerTeam.None);
            }else{
                return false;
            }
            sender.sendMessage("Done!");
            return true;
        }catch (Exception e){

        }
        return false;
    }
}
