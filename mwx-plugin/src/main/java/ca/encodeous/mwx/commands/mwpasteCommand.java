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

public class mwpasteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                Vector v = new Vector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                if(CoreGame.Instance.mwMissiles.containsKey(args[3])){
                    Missile mws = CoreGame.Instance.mwMissiles.get(args[3]);
                    CoreGame.Instance.mwImpl.PlaceMissile(mws, v, p.getWorld(), args[4].equals("red"), false);
                }else{
                    p.sendMessage(Formatter.FCL("&cMissile Not Found!"));
                }
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}
