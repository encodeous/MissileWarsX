package ca.encodeous.mwx.command;

import org.bukkit.command.CommandExecutor;

public abstract class MissileWarsCommand implements CommandExecutor {

    public abstract RootCommand BuildCommand();
    public abstract String GetCommandName();
}
