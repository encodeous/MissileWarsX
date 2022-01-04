package ca.encodeous.mwx.command;

import dev.jorel.commandapi.CommandAPICommand;

public interface RegisterCallback {
    CommandAPICommand run(CommandAPICommand cmd);
}
