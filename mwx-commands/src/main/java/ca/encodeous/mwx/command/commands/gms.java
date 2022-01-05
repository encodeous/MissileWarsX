package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class gms extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("gms", Command::DefaultRestrictedCommand, "s").Executes((context) -> {
            Player p = context.GetSendingPlayer();
            if(!Utils.CheckPrivPermission(p)) return 0;
            p.setGameMode(GameMode.SURVIVAL);
            context.SendMessage(String.format(Strings.GAMEMODE_UPDATED, p.getGameMode().name()));
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "mode";
    }
}
