package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class mwstartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(CoreGame.Instance.mwMatch.isStarting){
                sender.sendMessage("The game is already starting!");
            }else if(!CoreGame.Instance.mwMatch.hasStarted){
                CoreGame.Instance.mwMatch.isStarting = true;
                CoreGame.Instance.mwMatch.CountdownGame();
                CoreGame.Instance.mwMatch.mwCnt = 5;
            }else{
                sender.sendMessage("Game already started!");
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
