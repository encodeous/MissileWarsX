package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectate extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("spectate").Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
            if(match.IsPlayerInTeam(context.GetPlayer(), PlayerTeam.Spectator)){
                match.AddPlayerToTeam(context.GetPlayer(), PlayerTeam.None);
            }else{
                match.AddPlayerToTeam(context.GetPlayer(), PlayerTeam.Spectator);
            }
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "spectate";
    }
}
