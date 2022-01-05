package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class start extends MissileWarsCommand {
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

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("start", ca.encodeous.mwx.command.Command::DefaultPlayerCommand).Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
            Lobby lobby = match != null ? match.lobby : null;
            if(lobby == null) {
                context.SendMessage(Strings.LOBBY_COMMAND);
                return 0;
            }
            if(match instanceof MissileWarsRankedMatch){
                context.SendMessage("&cRanked games cannot be forcibly started.");
                return 0;
            }
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                context.SendMessage("&cThe game cannot be started at this time!");
                return 0;
            }else if(match.hasStarted){
                context.SendMessage("&cGame already started!");
                return 0;
            }else {
                match.startCounter.Start();
                return 1;
            }
        });
    }

    @Override
    public String GetCommandName() {
        return "start";
    }
}
