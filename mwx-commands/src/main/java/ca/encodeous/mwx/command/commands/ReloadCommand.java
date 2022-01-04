package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            CoreGame.Instance.Reload();
            sender.sendMessage(Chat.FCL("Successfully reloaded game"));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwreload", "Reloads MissileWarsX (Console only)", false).Create(e ->
                e.withPermission(core.GetMwxAdminPermission())
                        .executesConsole((p, args) -> {
                    CoreGame.Instance.Reload();
                    p.sendMessage(Chat.FCL("Successfully reloaded game"));
                }));
    }
}
