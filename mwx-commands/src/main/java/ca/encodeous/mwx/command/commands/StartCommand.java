package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandContext;
import ca.encodeous.mwx.command.CommandNode;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwstart", ca.encodeous.mwx.command.Command::DefaultPlayerCommand, "start")
                .Executes(context -> {
                    MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
                    if (CheckCommandExec(context, match) == 1) {
                        match.startCounter.Start();
                        return 1;
                    } else return 0;
                })
                .SubCommand(CommandNode.Literal("now")
                        .Executes(context -> {
                            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
                            if (CheckCommandExec(context, match) == 1) {
                                match.BeginGame();
                                return 1;
                            } else return 0;
                        }));
    }

    private int CheckCommandExec(CommandContext context, MissileWarsMatch match){
        Lobby lobby = match != null ? match.lobby : null;
        if(lobby == null) {
            context.SendMessage(Strings.LOBBY_COMMAND);
            return 0;
        }
        if(match instanceof MissileWarsRankedMatch){
            context.SendMessage("&cRanked games cannot be forcibly started.");
            return 0;
        }
        if(match.Map.isBusy || match.endCounter.isRunning()){
            context.SendMessage("&cThe game cannot be started at this time!");
            return 0;
        }else if(match.hasStarted){
            context.SendMessage("&cGame already started!");
            return 0;
        }else {
            return 1;
        }
    }

    @Override
    public String GetCommandName() {
        return "mwstart";
    }
}
