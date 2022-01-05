package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;

public class ResetCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("reset", Command::DefaultAdminCommand, "end").Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
            Lobby lobby = match != null ? match.lobby : null;
            if(lobby == null) {
                context.SendMessage(Strings.LOBBY_COMMAND);
                return 0;
            }
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                context.SendMessage("&cThe game cannot be reset at this time!");
                return 0;
            }
            match.EndGame();
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "reset";
    }
}
