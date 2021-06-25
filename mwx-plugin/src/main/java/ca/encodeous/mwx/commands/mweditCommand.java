package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class mweditCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                if(args[0].equals("auto")){
                    p.teleport(CoreGame.Instance.mwAuto.getSpawnLocation());
                }else if(args[0].equals("manual")){
                    p.teleport(CoreGame.Instance.mwManual.getSpawnLocation());
                }else return false;
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}
