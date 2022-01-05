package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map.Entry;

import static ca.encodeous.mwx.command.CommandSubCommand.*;

public class PasteCommand extends MissileWarsCommand {

    public int PasteMissile(CommandSender cs, Vector pos, String missile, boolean isRed, boolean update, World world){
        if(CoreGame.Instance.mwMissiles.containsKey(missile)){
            Missile mws = CoreGame.Instance.mwMissiles.get(missile);
            Player p = (cs instanceof Player pl) ? pl : null;
            boolean result = CoreGame.GetStructureManager().PlaceMissile(mws, pos, world, isRed, update, p);
            if(!result){
                MissileWarsMatch.SendCannotPlaceMessage(cs);
            }
            return 1;
        }else{
            return 0;
        }
    }

    private CommandSubCommand GetCommandFor(String missileName, String teamName, boolean isRed){
        return Literal(teamName)
                .SubCommand(
                        Boolean("updateBlocks")
                                .Executes(ExecutionSource.HAS_WORLD, context -> {
                                    return PasteMissile(context.GetSender(), context.GetPosition("anchor").toVector(),
                                            missileName, isRed, context.GetBoolean("updateBlocks"), context.GetSendingWorld());
                                })
                );
    }

    private void AddMissiles(CommandSubCommand cmd){
        for(var missileEntry : CoreGame.Instance.mwMissiles.entrySet()) {
            var name = missileEntry.getKey();
            cmd.SubCommand(Literal(name)
                            .SubCommand(GetCommandFor(name, "fromRed", true))
                            .SubCommand(GetCommandFor(name, "fromGreen", false))
            );
        }
    }

    @Override
    public RootCommand BuildCommand() {
        var loc = Position3d("anchor");
        AddMissiles(loc);
        return new RootCommand("mwpaste", Command::DefaultRestrictedCommand)
                .SubCommand(loc);
    }

    @Override
    public String GetCommandName() {
        return "mwpaste";
    }
}
