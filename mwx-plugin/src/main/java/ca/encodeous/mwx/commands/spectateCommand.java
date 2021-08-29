package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import lobbyengine.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(LobbyEngine.FromPlayer((Player) sender) != null){
                    MissileWarsMatch match = LobbyEngine.FromPlayer((Player) sender);
                    if(match.IsPlayerInTeam((Player) sender, PlayerTeam.Spectator)){
                        match.AddPlayerToTeam((Player) sender, PlayerTeam.None);
                    }else{
                        match.AddPlayerToTeam((Player) sender, PlayerTeam.Spectator);
                    }
                }
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
