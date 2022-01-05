package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;

public class start extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("start", ca.encodeous.mwx.command.Command::FunctionPermissionLevel).Executes(context -> {
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
