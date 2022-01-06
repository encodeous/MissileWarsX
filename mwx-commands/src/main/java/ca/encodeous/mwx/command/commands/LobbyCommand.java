package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.CommandNode;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;

public class LobbyCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwlobby", Command::DefaultPlayerCommand, "l", "mw", "leave", "lobby")
                .Executes(context -> {
                    MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
                    match.RemovePlayer(context.GetSendingPlayer());
                    match.AddPlayerToTeam(context.GetSendingPlayer(), PlayerTeam.None);
                    return 1;
                })
                .SubCommand(
                        CommandNode.Integer("lobby", 1, CoreGame.Instance.mwLobbies.Lobbies.size())
                                .Executes(context -> {
                                    int lobby = context.GetInteger("lobby");
                                    if(lobby > LobbyEngine.Lobbies.size()){
                                        context.SendMessage("&cThe lobby you specified does not exist.");
                                        return 0;
                                    }else{
                                        LobbyEngine.FromPlayer(context.GetSendingPlayer()).lobby.RemovePlayer(context.GetSendingPlayer());
                                        LobbyEngine.GetLobby(lobby).AddPlayer(context.GetSendingPlayer());
                                        context.SendMessage("&9You have been teleported to lobby " + lobby + ".");
                                        return 1;
                                    }
                                })
                );
    }

    @Override
    public String GetCommandName() {
        return "mwlobby";
    }
}
