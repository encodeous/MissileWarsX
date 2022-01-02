package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class mwitemsCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
    public void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder) {

    }

    @Override
    public String GetCommandName() {
        return "mwitems";
    }
}
