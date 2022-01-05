package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Map.Entry;

import static ca.encodeous.mwx.command.CommandSubCommand.*;

public class mwpaste extends MissileWarsCommand {

    private int spawn(CommandContext context, Missile missile, boolean red) throws CommandSyntaxException {
        boolean result = CoreGame.GetStructureManager().PlaceMissile(missile, context.GetPosition("pivot").toVector(), context.GetPlayer().getWorld(), red, false, context.GetPlayer());
        if(!result){
            MissileWarsMatch.SendCannotPlaceMessage(context.GetPlayer());
            return 0;
        }
        return 1;
    }

    private CommandSubCommand addMissiles(CommandSubCommand cmd) {
        for(Entry<String, Missile> missileEntry : CoreGame.Instance.mwMissiles.entrySet()) {
            cmd.SubCommand(Literal(missileEntry.getKey())
                    .SubCommand(Literal("red").Executes(context -> spawn(context, missileEntry.getValue(), true)))
                    .SubCommand(Literal("green").Executes(context -> spawn(context, missileEntry.getValue(), false)))
            );
        }
        return cmd;
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwpaste", Command::FunctionPermissionLevel)
                .SubCommand(addMissiles(Position3d("pivot")));
    }

    @Override
    public String GetCommandName() {
        return "mwpaste";
    }
}
