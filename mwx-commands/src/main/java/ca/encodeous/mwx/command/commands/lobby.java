package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.CommandSubCommand;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;

public class lobby extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("lobby","l", "mw", "leave")
                .Executes(context -> {
                    MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
                    match.RemovePlayer(context.GetPlayer());
                    match.AddPlayerToTeam(context.GetPlayer(), PlayerTeam.None);
                    return 1;
                })
                .SubCommand(
                        CommandSubCommand.Integer("lobby", 1)
                                .Executes(context -> {
                                    int lobby = context.GetInteger("lobby");
                                    if(lobby > LobbyEngine.Lobbies.size()){
                                        context.SendMessage("&cThe lobby you specified does not exist.");
                                        return 0;
                                    }else{
                                        LobbyEngine.FromPlayer(context.GetPlayer()).lobby.RemovePlayer(context.GetPlayer());
                                        LobbyEngine.GetLobby(lobby).AddPlayer(context.GetPlayer());
                                        context.SendMessage("&9You have been teleported to lobby " + lobby + ".");
                                        return 1;
                                    }
                                })
                );
    }

    @Override
    public String GetCommandName() {
        return "lobby";
    }
}
