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

public class mwendCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            // DEBUGGING
//            CoreGame.GetMatch().ResetWorld();
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
                sender.sendMessage("The game cannot be ended at this time!");
                return true;
            }
            if(match.hasStarted){
                for(Player p : Bukkit.getOnlinePlayers()){
                    CoreGame.GetImpl().SendTitle(p, "&9The game has been ended.", "&9Stopped by an Admin.");
                }
                //match.EndGame();
            }else{
                Bukkit.broadcastMessage(Chat.FCL("&9Resetting game...!"));
                //CoreGame.Instance.EndMatch();
            }

            return true;
        }catch (Exception e){
            return false;
        }
    }
}
