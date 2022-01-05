package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.ExecutionSource;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;

public class ReloadCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwreload", Command::DefaultAdminCommand).Executes(ExecutionSource.CONSOLE, (context) -> {
            CoreGame.Instance.Reload();
            context.SendMessage("&aSuccessfully reloaded game");
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "mwreload";
    }
}
