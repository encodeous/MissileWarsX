package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.entity.Player;

import java.util.Collection;

import static ca.encodeous.mwx.command.CommandSubCommand.Literal;
import static ca.encodeous.mwx.command.CommandSubCommand.PlayerSingle;

public class TeamCommand extends MissileWarsCommand {

    public PlayerTeam GetTeam(String name){
        return switch (name){
            case "red" -> PlayerTeam.Red;
            case "green" -> PlayerTeam.Green;
            case "spectator" -> PlayerTeam.Spectator;
            case "lobby" -> PlayerTeam.None;
            default -> null;
        };
    }

    private void CompleteTeamSwitch(MissileWarsMatch sourceMatch, Collection<Player> entities, PlayerTeam team){
        for(Player entity : entities){
            MissileWarsMatch match2 = LobbyEngine.FromPlayer(entity);
            if(sourceMatch != match2) continue;
            match2.AddPlayerToTeam(entity, team);
        }
    }

    private CommandSubCommand CreateLiteralForTeam(String name){
        return Literal(name)
                .Executes(context -> {
                    var tgt = context.GetPlayers("targets");
                    var src = LobbyEngine.FromPlayer(context.GetSendingPlayer());
                    CompleteTeamSwitch(src, tgt, GetTeam(name));
                    return 1;
                });
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwteam", Command::DefaultRestrictedCommand, "mwt")
                .SubCommand(PlayerSingle("targets")
                        .SubCommand(CreateLiteralForTeam("green"))
                        .SubCommand(CreateLiteralForTeam("red"))
                        .SubCommand(CreateLiteralForTeam("lobby"))
                        .SubCommand(CreateLiteralForTeam("spectator"))
                );
    }

    @Override
    public String GetCommandName() {
        return "mwteam";
    }
}
