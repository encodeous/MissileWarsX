package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;

public class SpectateCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwspectate", Command::DefaultPlayerCommand, "spectate", "spec").Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
            if(match.IsPlayerInTeam(context.GetSendingPlayer(), PlayerTeam.Spectator)){
                match.AddPlayerToTeam(context.GetSendingPlayer(), PlayerTeam.None);
            }else{
                match.AddPlayerToTeam(context.GetSendingPlayer(), PlayerTeam.Spectator);
            }
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "mwspectate";
    }
}
