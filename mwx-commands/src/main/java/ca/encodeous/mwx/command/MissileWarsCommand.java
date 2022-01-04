package ca.encodeous.mwx.command;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.command.CommandExecutor;

public abstract class MissileWarsCommand implements CommandExecutor {

    public abstract void BuildCommand(CommandCore core);
}
