package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.configuration.MissileSchematic;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static ca.encodeous.mwx.command.CommandSubCommand.*;

public class MakeCommand extends MissileWarsCommand {

    public boolean MakeMissile(Player p, Vector pos1, Vector pos2, Vector pivot, String name, boolean isRed){
        if(CoreGame.Instance.mwMissiles.containsKey(name)){
            return false;
        }
        MissileSchematic schem = CoreGame.GetStructureManager().GetSchematic(pivot, Bounds.of(pos1,pos2), p.getWorld());
        if(schem == null){
            p.sendMessage(Chat.FCL("&cFailed creating missile! Valid blocks are: pistons, glass, stained glass, slime blocks, tnt, redstone blocks, terracotta / stained clay. Schematics must also not be empty!"));
            return true;
        }
        if(!isRed){
            schem = schem.CreateOppositeSchematic();
        }
        Missile missile = new Missile();
        missile.Schematic = schem;
        missile.MissileItemId = name;
        CoreGame.Instance.mwMissiles.put(missile.MissileItemId, missile);
        p.sendMessage(Chat.FCL("&aSuccessfully created missile!"));
        return true;
    }

    private CommandSubCommand GetCommandFor(String teamName, boolean isRed){
        return Literal(teamName)
                .Executes(context -> {
                    var name = context.GetString("missileName");
                    var pos1 = context.GetPosition("pos1");
                    var pos2 = context.GetPosition("pos2");
                    var anchor = context.GetPosition("anchor");
                    if(!MakeMissile(context.GetSendingPlayer(), pos1.toVector(), pos2.toVector(), anchor.toVector(), name, isRed)){
                        context.SendMessage("&cThe specified missile name already exists! Delete the yml file before continuing.");
                        return 0;
                    }
                    return 1;
                });
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwmake", Command::DefaultAdminCommand)
                .SubCommand(Position3d("pos1")
                        .SubCommand(Position3d("pos2")
                                .SubCommand(Position3d("anchor")
                                        .SubCommand(Word("missileName")
                                                .SubCommand(GetCommandFor("fromGreen", false)
                                                        .SubCommand(GetCommandFor("fromRed", true)))))));
    }

    @Override
    public String GetCommandName() {
        return "mwmake";
    }
}
