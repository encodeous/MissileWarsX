package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class mwitemsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            List<String> results = CoreGame.Instance.mwConfig.Items.stream().map(x->{
                return "&cId: "+x.MissileWarsItemId + " - Stack Size: " + x.StackSize + " - Max Stack Size: " + x.MaxStackSize;
            }).collect(Collectors.toList());
            String result = Formatter.FCL(String.join("\n", results));
            sender.sendMessage(result);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
