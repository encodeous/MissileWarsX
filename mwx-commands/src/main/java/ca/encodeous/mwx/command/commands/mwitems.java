package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

import static ca.encodeous.mwx.command.CommandExecutionRequirement.NONE;

public class mwitems extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            List<String> results = CoreGame.Instance.mwConfig.Items.stream().map(x->{
                return "&cId: "+x.MissileWarsItemId + " - Stack Size: " + x.StackSize + " - Max Stack Size: " + x.MaxStackSize;
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
        return new RootCommand("mwitems").Executes(NONE, context -> {
            List<String> results = CoreGame.Instance.mwConfig.Items.stream()
                    .map(x -> "&cId: "+x.MissileWarsItemId + " - Stack Size: " + x.StackSize + " - Max Stack Size: " + x.MaxStackSize)
                    .collect(Collectors.toList());

            String result = String.join("\n", results);
            context.SendMessage(result);
            return results.size();
        });
    }

    @Override
    public String GetCommandName() {
        return "mwitems";
    }
}
