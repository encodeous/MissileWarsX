package ca.encodeous.mwx.mwxcompat1_17.CommandAPI;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.plugin.Plugin;

public class CommandCore extends ca.encodeous.mwx.command.CommandCore {
    @Override
    public CommandAPICommand GetCommand(String name) {
        return new CommandAPICommand(name);
    }
    @Override
    public void CreateCommand(CommandAPICommand command) {
        command.register();
    }
}
