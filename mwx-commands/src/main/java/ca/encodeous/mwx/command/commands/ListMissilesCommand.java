package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ListMissilesCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            SendMissileList(sender);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void SendMissileList(CommandSender sender){
        List<String> results = CoreGame.Instance.mwMissiles.values().stream().map(x->{
            return "&cId: "+x.MissileItemId;
        }).collect(Collectors.toList());
        String result = Chat.FCL(String.join("\n", results));
        sender.sendMessage(result);
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwmissiles", "Lists all the missiles", false).Create(e ->
                e.executes((p, args) -> {
                    SendMissileList(p);
                }));
    }
}
