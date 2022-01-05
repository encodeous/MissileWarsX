package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;

public class ping extends MissileWarsCommand {

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
