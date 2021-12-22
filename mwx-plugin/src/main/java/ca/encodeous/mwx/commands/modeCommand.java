package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsPracticeMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.lang.Strings;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class modeCommand implements CommandExecutor {
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
}
