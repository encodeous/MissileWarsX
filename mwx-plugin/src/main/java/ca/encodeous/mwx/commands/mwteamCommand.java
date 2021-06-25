package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwteamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Player p = Bukkit.getPlayer(args[0]);
            if(p == null){
                sender.sendMessage("Player not found!");
                return true;
            }
            if(args[1].equals("green")){
                CoreGame.Instance.mwMatch.AddGreenPlayer(p);
            } else if(args[1].equals("red")){
                CoreGame.Instance.mwMatch.AddRedPlayer(p);
            } else if(args[1].equals("spectator")){
                CoreGame.Instance.mwMatch.AddSpectator(p);
            } else if(args[1].equals("lobby")){
                CoreGame.Instance.mwMatch.AddPlayerToLobby(p);
            }else{
                return false;
            }
            sender.sendMessage("Done!");
            return true;
        }catch (Exception e){

        }
        return false;
    }
}
