package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.lang.Strings;

public class ready extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("ready", Command::DefaultPlayerCommand).Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
            Lobby lobby = match != null ? match.lobby : null;
            if(lobby == null) {
                context.SendMessage(Strings.LOBBY_COMMAND);
                return 0;
            }
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning() || match.hasStarted){
                context.SendMessage("&cYou cannot run that command at this time");
                return 0;
            }
            if(!(match instanceof MissileWarsRankedMatch rankedMatch)){
                context.SendMessage("&cThis command can only be run in a ranked game!");
                return 0;
            }
            if(!match.Red.contains(context.GetPlayer()) && !match.Green.contains(context.GetPlayer())){
                context.SendMessage("&cYou must be in a team to run this command");
                return 0;
            }
            if(match.Red.contains(context.GetPlayer())){
                rankedMatch.TeamReady(PlayerTeam.Red);
            }else{
                rankedMatch.TeamReady(PlayerTeam.Green);
            }
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "ready";
    }
}
