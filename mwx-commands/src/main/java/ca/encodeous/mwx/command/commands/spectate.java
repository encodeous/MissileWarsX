package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;

public class spectate extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("spectate").Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
            if(match.IsPlayerInTeam(context.GetPlayer(), PlayerTeam.Spectator)){
                match.AddPlayerToTeam(context.GetPlayer(), PlayerTeam.None);
            }else{
                match.AddPlayerToTeam(context.GetPlayer(), PlayerTeam.Spectator);
            }
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "spectate";
    }
}
