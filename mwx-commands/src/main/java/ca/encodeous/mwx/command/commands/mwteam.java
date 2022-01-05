package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Player;

import static ca.encodeous.mwx.command.CommandExecutionRequirement.NONE;
import static ca.encodeous.mwx.command.CommandSubCommand.Literal;
import static ca.encodeous.mwx.command.CommandSubCommand.PlayerSingle;

public class mwteam extends MissileWarsCommand {

    private int handleCommand(CommandContext context, String team) throws CommandSyntaxException {
        try {
            Player player = context.GetPlayer("selector");
            MissileWarsMatch match = LobbyEngine.FromPlayer(player);
            Lobby lobby = match != null ? match.lobby : null;
            if (lobby == null) {
                context.SendMessage(Strings.LOBBY_COMMAND);
                return 0;
            }
            if (match instanceof MissileWarsRankedMatch) {
                context.SendMessage("&cCannot switch teams in a ranked game.");
                return 0;
            }
            switch (team) {
                case "green" -> match.AddPlayerToTeam(player, PlayerTeam.Green);
                case "red" -> match.AddPlayerToTeam(player, PlayerTeam.Red);
                case "spectator" -> match.AddPlayerToTeam(player, PlayerTeam.Spectator);
                case "lobby" -> match.AddPlayerToTeam(player, PlayerTeam.None);
            }
            context.SendMessage("&aDone!");
            return 1;
        } catch(Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwteam", Command::DefaultPlayerCommand, "mwt")
                .SubCommand(PlayerSingle("selector")
                        .SubCommand(Literal("green").Executes(NONE, (context) -> handleCommand(context, "green")))
                        .SubCommand(Literal("red").Executes(NONE, (context) -> handleCommand(context, "red")))
                        .SubCommand(Literal("spectator").Executes(NONE, (context) -> handleCommand(context, "spectator")))
                        .SubCommand(Literal("lobby").Executes(NONE, (context) -> handleCommand(context, "lobby"))));
    }

    @Override
    public String GetCommandName() {
        return "mwteam";
    }
}
