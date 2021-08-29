package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import lobbyengine.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class lobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                LobbyEngine.FromPlayer((Player) sender).AddPlayerToTeam((Player) sender, PlayerTeam.None);
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
