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
            if(CoreGame.Instance.mwMatch.hasStarted){
                for(Player p : Bukkit.getOnlinePlayers()){
                    CoreGame.Instance.mwImpl.SendTitle(p, "&9The game has been ended.", "&9Stopped by an Admin.");
                }
                CoreGame.Instance.mwMatch.EndGame();
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
