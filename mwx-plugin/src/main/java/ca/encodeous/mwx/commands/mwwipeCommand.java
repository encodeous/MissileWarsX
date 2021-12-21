package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.lobbyengine.Lobby;
import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwwipeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Lobby lobby = null;
            if(sender instanceof Player){
                if(LobbyEngine.FromPlayer((Player) sender) != null){
                    lobby = LobbyEngine.FromPlayer((Player) sender).lobby;
                }
            }
            Player cur = (Player) sender;
            if(lobby == null){
                lobby = LobbyEngine.GetLobby(0);
            }
            MissileWarsMatch match = lobby.Match;
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                sender.sendMessage("The map cannot be wiped at this time!");
                return true;
            }
            for(Player p : match.lobby.GetPlayers()){
                CoreGame.GetImpl().SendTitle(p, "&9The map is being wiped", "&9by " + cur.getDisplayName()+"&r.");
            }
            Lobby finalLobby = lobby;
            match.Wipe(()->{
                finalLobby.SendMessage("&9The lobby has been wiped");
            });
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
