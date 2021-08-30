package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import lobbyengine.Lobby;
import lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwresetCommand implements CommandExecutor {
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
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                sender.sendMessage("The game cannot be reset at this time!");
                return true;
            }
            for(Player p : match.lobby.GetPlayers()){
                CoreGame.GetImpl().SendTitle(p, "&9The game has been reset", "&9by an Admin.");
            }
            match.StartResetting();
            match.endCounter.Start();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
