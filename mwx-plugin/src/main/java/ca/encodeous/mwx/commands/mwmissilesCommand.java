package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class mwmissilesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            List<String> results = CoreGame.Instance.mwMissiles.values().stream().map(x->{
                return "&cId: "+x.MissileItemId;
            }).collect(Collectors.toList());
            String result = Chat.FCL(String.join("\n", results));
            sender.sendMessage(result);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
