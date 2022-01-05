package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.ExecutionSource;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;

import static ca.encodeous.mwx.command.CommandSubCommand.PlayerSingle;

public class PingCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwping", "ping")
                .SubCommand(PlayerSingle("target")
                        .Executes(ExecutionSource.ANY, context -> {
                            var player = context.GetPlayer("target");
                            context.SendMessage(String.format(Strings.PING_OTHER_MESSAGE, player.getName(), Utils.GetPlayerPing(player)));
                            return 1;
                        }))
                .Executes(context -> {
                    context.SendMessage(String.format(Strings.PING_MESSAGE, Utils.GetPlayerPing(context.GetSendingPlayer())));
                    return 1;
                });
    }

    @Override
    public String GetCommandName() {
        return "mwping";
    }
}
