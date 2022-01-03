package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class pingCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            sender.sendMessage(String.format(Strings.PING_MESSAGE, Utils.GetPlayerPing((Player) sender)));
        }
        else{
            sender.sendMessage(Strings.NOT_PLAYER);
        }
        return true;
    }

    @Override
    public RootCommand BuildCommand() {
        throw new NotImplementedException("Building this command is not implemented");
    }

    @Override
    public String GetCommandName() {
        return "ping";
    }
}
