package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.command.CommandSender;

public class mwreload extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            CoreGame.Instance.Reload();
            sender.sendMessage(Chat.FCL("Successfully reloaded game"));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwreload", Command::DefaultAdminCommand).Executes((context) -> {
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
