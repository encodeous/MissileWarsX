package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.ExecutionSource;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Location;

import static ca.encodeous.mwx.command.CommandNode.Position3d;

public class FireballCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwfireball", Command::DefaultRestrictedCommand, "mwf", "fireball")
                .SubCommand(Position3d("location")
                        .Executes(ExecutionSource.HAS_WORLD, (context) -> {
                    MissileWarsMatch match = LobbyEngine.FromWorld(context.GetSendingWorld());
                    if(match == null) {
                        context.SendMessage("&cYou cannot summon a fireball");
                        return 0;
                    }
                    Location loc = context.GetPosition("location");
                    match.DeployFireball(loc.getBlock(), new Ref<>(false), new Ref<>(false), context.GetSendingPlayer());
                    context.SendMessage("&aFireball summoned at the specified location.");
                    return 1;
                }));
    }

    @Override
    public String GetCommandName() {
        return "mwfireball";
    }
}
