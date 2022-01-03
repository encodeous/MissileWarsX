package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.MissileWarsCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwstartCommand extends MissileWarsCommand {
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
                sender.sendMessage(Strings.LOBBY_COMMAND);
                return true;
            }
            MissileWarsMatch match = lobby.Match;
            if(match instanceof MissileWarsRankedMatch){
                sender.sendMessage("Ranked games cannot be forcibly started.");
                return true;
            }
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                sender.sendMessage("The game cannot be started at this time!");
            }else if(!match.hasStarted){
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
