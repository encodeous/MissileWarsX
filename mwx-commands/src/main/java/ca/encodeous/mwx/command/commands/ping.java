package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ping extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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
        return new RootCommand("ping").Executes((context) -> {
            int ping = Utils.GetPlayerPing(context.GetPlayer());
            context.SendMessage(String.format(Strings.PING_MESSAGE, ping));
            return ping;
        });
    }

    @Override
    public String GetCommandName() {
        return "ping";
    }
}
