package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class ListItemsCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            SendItemList(sender);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void SendItemList(CommandSender cs){
        List<String> results = CoreGame.Instance.mwConfig.Items.stream().map(x->{
            return "&cId: "+x.MissileWarsItemId + " - Stack Size: " + x.StackSize + " - Max Stack Size: " + x.MaxStackSize;
        }).collect(Collectors.toList());
        String result = Chat.FCL(String.join("\n", results));
        cs.sendMessage(result);
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwitems", "Lists all the items", false).Create(e ->
                e.executes((p, args) -> {
                    SendItemList(p);
                }));
    }
}
