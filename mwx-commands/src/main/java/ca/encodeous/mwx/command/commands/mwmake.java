package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.configuration.MissileSchematic;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static ca.encodeous.mwx.command.CommandSubCommand.Position3d;
import static ca.encodeous.mwx.command.CommandSubCommand.Word;

public class mwmake extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(CoreGame.Instance.mwMissiles.containsKey(args[9])){
                    CoreGame.Instance.mwMissiles.remove(args[9]);
                }
                Player p = (Player) sender;
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
                MissileSchematic schem = CoreGame.GetStructureManager().GetSchematic(pivot, Bounds.of(a,b), p.getWorld());
                if(schem == null){
                    p.sendMessage(Chat.FCL("&cFailed creating missile! Valid blocks are: [PISTON, GLASS, STAINED_GLASS, SLIME_BLOCK, TNT, REDSTONE_BLOCK, STAINED_CLAY]. Schematics must also not be empty!"));
                    return true;
                }
                Missile missile = new Missile();
                missile.Schematic = schem;
                missile.MissileItemId = name;
                CoreGame.Instance.mwMissiles.put(missile.MissileItemId, missile);
                p.sendMessage(Chat.FCL("&aSuccessfully created missile!"));
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwmake", Command::HighestPermissionLevel)
                .SubCommand(Position3d("pivot")
                        .SubCommand(Position3d("sel")
                                .SubCommand(Position3d("sel2")
                                        .SubCommand(Word("missile").Executes((context) -> {
                                            String missileName = context.GetString("missile");
                                            if(CoreGame.Instance.mwMissiles.containsKey(missileName))
                                                CoreGame.Instance.mwMissiles.remove(missileName);
                                            Location pivot = context.GetPosition("pivot");
                                            Location a = context.GetPosition("sel");
                                            Location b = context.GetPosition("sel2");
                                            MissileSchematic schem = CoreGame.GetStructureManager().GetSchematic(pivot.toVector(), Bounds.of(a.toVector(), b.toVector()), context.GetPlayer().getWorld());
                                            if(schem == null){
                                                context.SendMessage("&cFailed creating missile! Valid blocks are: [PISTON, GLASS, STAINED_GLASS, SLIME_BLOCK, TNT, REDSTONE_BLOCK, STAINED_CLAY]. Schematics must also not be empty!");
                                                return 0;
                                            }
                                            Missile missile = new Missile();
                                            missile.Schematic = schem;
                                            missile.MissileItemId = missileName;
                                            CoreGame.Instance.mwMissiles.put(missile.MissileItemId, missile);
                                            context.SendMessage(Chat.FCL("&aSuccessfully created missile!"));
                                            return 1;
                                        })))));
    }

    @Override
    public String GetCommandName() {
        return "mwmake";
    }
}
