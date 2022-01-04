package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PingCommand extends MissileWarsCommand {
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
    public void BuildCommand(CommandCore core) {
        new CommandRegister("ping", "Gets the latency to the server", false).Create(e ->
                e.withAliases("mwping")
                        .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                        .executesNative((sender, args) -> {
                            if(args[0] instanceof Player p){
                                if(sender == args[0]){
                                    sender.sendMessage(String.format(Strings.PING_MESSAGE, Utils.GetPlayerPing(p)));
                                }else{
                                    sender.sendMessage(String.format(Strings.PING_OTHER_MESSAGE, p.getName(), Utils.GetPlayerPing(p)));
                                }
                            }
                        }));
    }
}
