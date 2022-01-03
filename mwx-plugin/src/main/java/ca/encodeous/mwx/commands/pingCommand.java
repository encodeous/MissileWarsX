package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.MissileWarsCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class pingCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            sender.sendMessage(String.format(Strings.PING_MESSAGE, Utils.GetPlayerPing((Player) sender)));
        }
        else{
            sender.sendMessage(Strings.NOT_PLAYER);
        }
        return true;
    }
}
