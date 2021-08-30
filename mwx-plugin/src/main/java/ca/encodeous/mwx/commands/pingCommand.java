package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import util.SpigotReflection;

public class pingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            sender.sendMessage(Chat.FCL("&6Your ping is &2"+ Utils.GetPlayerPing((Player) sender) +" &6ms."));
        }
        else{
            sender.sendMessage("You are not a player...");
        }
        return true;
    }
}
