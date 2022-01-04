package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.configuration.MissileSchematic;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.StringArgument;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MakeCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player p){
                Vector pivot = new Vector(
                        Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]));
                Vector a = new Vector(
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4]),
                        Integer.parseInt(args[5]));
                Vector b = new Vector(
                        Integer.parseInt(args[6]),
                        Integer.parseInt(args[7]),
                        Integer.parseInt(args[8]));
                String name = args[9];
                if(!MakeMissile(p, a, b, pivot, name, true)){
                    p.sendMessage(Chat.FCL("&cThe missile \"" + name + "\" already exists! Please delete \"" + name + ".yml\" before trying to create it."));
                }
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

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

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwmake", "Creates a missile from world blocks", true).Create(e ->
                e.withArguments(new LocationArgument("pos1", LocationType.BLOCK_POSITION),
                        new LocationArgument("pos2", LocationType.BLOCK_POSITION),
                                new LocationArgument("anchor", LocationType.BLOCK_POSITION),
                                new StringArgument("missileName"),
                                new StringArgument("facing").replaceSuggestions(x->new String[]{
                                        "fromRed",
                                        "fromGreen"
                                }))
                        .withPermission(core.GetMwxAdminPermission())
                        .executesPlayer((p, args) -> {
                            var pos1 = (Location) args[0];
                            var pos2 = (Location) args[1];
                            var anchor = (Location) args[2];
                            var name = (String) args[3];
                            var facing = (String) args[4];
                            if(!facing.equals("fromRed") && !facing.equals("fromGreen")){
                                CommandAPI.fail("The specified facing is invalid");
                                return;
                            }
                            boolean isRed = facing.equals("fromRed");
                            if(!MakeMissile(p, pos1.toVector(), pos2.toVector(), anchor.toVector(), name, isRed)){
                                CommandAPI.fail("The specified missile name already exists! Delete the yml file before continuing.");
                            }
                        })
        );
    }
}
