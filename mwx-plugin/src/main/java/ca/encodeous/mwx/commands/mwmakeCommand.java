package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import ca.encodeous.mwx.mwxplugin.MissileWarsX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class mwmakeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                MissileSchematic schem = MissileWarsX.Instance.mwImpl.GetStructureManager().GetSchematic(pivot, Bounds.of(a,b), p.getWorld());
                if(schem == null){
                    p.sendMessage(Formatter.FCL("&cFailed creating missile! Valid blocks are: [PISTON, GLASS, STAINED_GLASS, SLIME_BLOCK, TNT, REDSTONE_BLOCK, STAINED_CLAY]. Schematics must also not be empty!"));
                    return true;
                }
                Missile missile = new Missile();
                missile.Schematic = schem;
                missile.MissileItemId = name;
                CoreGame.Instance.mwMissiles.put(missile.MissileItemId, missile);
                p.sendMessage(Formatter.FCL("&aSuccessfully created missile!"));
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}
