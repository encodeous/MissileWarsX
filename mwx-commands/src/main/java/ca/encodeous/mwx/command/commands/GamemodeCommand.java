package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(args.length == 0){
                    Player p = (Player) sender;
                    if(!Utils.CheckPrivPermission(p)) return true;
                    if(p.getGameMode() == GameMode.CREATIVE){
                        p.setGameMode(GameMode.SURVIVAL);
                    }else{
                        p.setGameMode(GameMode.CREATIVE);
                    }
                    sender.sendMessage(String.format(Strings.GAMEMODE_UPDATED, p.getGameMode().name()));
                }
                else return false;
            }
            else{
                sender.sendMessage("You are not a player...");
            }
            return true;
        }catch (Exception e){

        }
        return false;
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("creative", "Sets your gamemode to creative mode", true).Create(e ->
                e.withAliases("gmc", "c", "mwcreative")
                        .executesPlayer((p, args) -> {
                            p.setGameMode(GameMode.CREATIVE);
                            p.sendMessage(String.format(Strings.GAMEMODE_UPDATED, p.getGameMode().name()));
                        })
        );
        new CommandRegister("survival", "Sets your gamemode to survival mode", true).Create(e ->
                e.withAliases("gms", "s", "mwsurvival")
                        .executesPlayer((p, args) -> {
                            p.setGameMode(GameMode.SURVIVAL);
                            p.sendMessage(String.format(Strings.GAMEMODE_UPDATED, p.getGameMode().name()));
                        })
        );
    }
}
