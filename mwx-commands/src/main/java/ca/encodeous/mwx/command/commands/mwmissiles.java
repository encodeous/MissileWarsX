package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class mwmissiles extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwmissiles").Executes((context) -> {
            List<String> results = CoreGame.Instance.mwMissiles.values().stream()
                    .map(x-> "&cId: " + x.MissileItemId)
                    .collect(Collectors.toList());
            String result = Chat.FCL(String.join("\n", results));
            context.SendMessage(result);
            return CoreGame.Instance.mwMissiles.size();
        });
    }

    @Override
    public String GetCommandName() {
        return "mwmissiles";
    }
}
