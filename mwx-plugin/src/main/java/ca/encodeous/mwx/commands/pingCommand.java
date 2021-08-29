package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import util.SpigotReflection;

public class pingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_17.getValue()){
                sender.sendMessage(Chat.FCL("&6Your ping is &2"+ p.getPing() +" &6ms."));
            }else{
                sender.sendMessage(Chat.FCL("&6Your ping is &2"+ SpigotReflection.get().ping(p) +" &6ms."));
            }
        }
        else{
            sender.sendMessage("You are not a player...");
        }
        return true;
    }
}
