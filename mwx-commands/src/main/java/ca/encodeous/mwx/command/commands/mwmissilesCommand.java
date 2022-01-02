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

public class mwmissilesCommand extends MissileWarsCommand {
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

    @Override
    public void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder) {

    }

    @Override
    public String GetCommandName() {
        return "mwmissiles";
    }
}
