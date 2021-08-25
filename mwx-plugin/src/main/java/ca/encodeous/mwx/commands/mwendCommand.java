package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwendCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(CoreGame.GetMatch().isStarting || CoreGame.GetMatch().isEnding){
                sender.sendMessage("The game cannot be ended at this time!");
                return true;
            }
            if(CoreGame.GetMatch().hasStarted){
                for(Player p : Bukkit.getOnlinePlayers()){
                    CoreGame.GetImpl().SendTitle(p, "&9The game has been ended.", "&9Stopped by an Admin.");
                }
                CoreGame.GetMatch().EndGame();
            }else{
                Bukkit.broadcastMessage(Formatter.FCL("&9Resetting game...!"));
                CoreGame.Instance.EndMatch();
            }

            return true;
        }catch (Exception e){
            return false;
        }
    }
}
