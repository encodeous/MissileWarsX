package ca.encodeous.mwx.mwxcompat1_17;

import ca.encodeous.mwx.engines.command.CommandBase;
import ca.encodeous.mwx.mwxcompat1_13.CommandAPI.CommandCore;
import dev.jorel.commandapi.CommandAPICommand;

public class MissileWars1_17 extends ca.encodeous.mwx.mwxcompat1_13.MissileWars1_13 {
    private static CommandBase commandCore = new ca.encodeous.mwx.mwxcompat1_17.CommandAPI.CommandCore();
    @Override
    public CommandBase GetCommandCore() {
        return commandCore;
    }
}
