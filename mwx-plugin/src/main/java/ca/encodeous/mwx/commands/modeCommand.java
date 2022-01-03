package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.MissileWarsCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class modeCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(args.length == 0){
                    Player p = (Player) sender;
                    if(!Utils.CheckPrivPermission(p)) return true;
                    if(p.getGameMode() == GameMode.CREATIVE){
                        p.setGameMode(GameMode.SURVIVAL);
                    }else{
                        p.setGameMode(GameMode.CREATIVE);
                    }
                    sender.sendMessage(String.format(Strings.GAMEMODE_UPDATED, p.getGameMode().name()));
                }
                else return false;
            }
            else{
                sender.sendMessage("You are not a player...");
            }
            return true;
        }catch (Exception e){

        }
        return false;
    }
}
