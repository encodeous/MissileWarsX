package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.lobbyengine.Lobby;
import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class readyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(!(sender instanceof Player)){
                sender.sendMessage("You are not a player!");
                return true;
            }
            Player p = (Player) sender;
            Lobby lobby = null;
            if(LobbyEngine.FromPlayer(p) != null){
                lobby = LobbyEngine.FromPlayer(p).lobby;
            }
            if(lobby == null){
                lobby = LobbyEngine.GetLobby(0);
            }
            MissileWarsMatch match = lobby.Match;
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning() || match.hasStarted){
                sender.sendMessage("You cannot run that command at this time");
                return true;
            }
            if(!match.isRanked){
                sender.sendMessage("This command can only be run in a ranked game!");
                return true;
            }
            if(!match.Red.contains(p) && !match.Green.contains(p)){
                sender.sendMessage("You must be in a team to run this command");
                return true;
            }
            if(match.Red.contains(p)){
                match.TeamReady(PlayerTeam.Red);
            }else{
                match.TeamReady(PlayerTeam.Green);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
