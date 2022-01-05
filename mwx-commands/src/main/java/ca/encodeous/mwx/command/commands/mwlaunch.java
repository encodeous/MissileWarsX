package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandContext;
import ca.encodeous.mwx.command.CommandSubCommand;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Map;

import static ca.encodeous.mwx.command.CommandSubCommand.Literal;
import static ca.encodeous.mwx.command.CommandSubCommand.Position3d;

public class mwlaunch extends MissileWarsCommand {

    private int spawn(CommandContext context, Missile missile, boolean red) throws CommandSyntaxException {
        boolean result = CoreGame.GetStructureManager().PlaceMissile(missile, context.GetPosition("pivot").toVector(), context.GetPlayer().getWorld(), red, true, context.GetPlayer());
        if(!result){
            MissileWarsMatch.SendCannotPlaceMessage(context.GetPlayer());
            return 0;
        }
        return 1;
    }

    private CommandSubCommand addMissiles(CommandSubCommand cmd) {
        for(Map.Entry<String, Missile> missileEntry : CoreGame.Instance.mwMissiles.entrySet()) {
            cmd.SubCommand(Literal(missileEntry.getKey())
                    .SubCommand(Literal("red").Executes(context -> spawn(context, missileEntry.getValue(), true)))
                    .SubCommand(Literal("green").Executes(context -> spawn(context, missileEntry.getValue(), false)))
            );
        }
        return cmd;
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwlaunch", ca.encodeous.mwx.command.Command::DefaultRestrictedCommand)
                .SubCommand(addMissiles(Position3d("pivot")));
    }

    @Override
    public String GetCommandName() {
        return "mwlaunch";
    }
}
