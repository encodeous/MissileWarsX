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
            if(CoreGame.GetMatch().isStarting || CoreGame.GetMatch().isEnding){
                sender.sendMessage("The game cannot be started at this time!");
            }else if(!CoreGame.GetMatch().hasStarted){
                CoreGame.GetMatch().isStarting = true;
                CoreGame.GetMatch().CountdownGame();
                CoreGame.GetMatch().mwCnt = 5;
            }else{
                sender.sendMessage("Game already started!");
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
