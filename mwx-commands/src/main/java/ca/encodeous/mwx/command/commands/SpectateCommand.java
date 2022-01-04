package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                SetSpectator((Player) sender);
            }
            else{
                sender.sendMessage("You are not a player...");
            }
            return true;
        }catch (Exception e){

        }
        return false;
    }

    public void SetSpectator(Player p){
        if(LobbyEngine.FromPlayer(p) != null){
            MissileWarsMatch match = LobbyEngine.FromPlayer(p);
            if(match.IsPlayerInTeam(p, PlayerTeam.Spectator)){
                match.AddPlayerToTeam(p, PlayerTeam.None);
            }else{
                match.AddPlayerToTeam(p, PlayerTeam.Spectator);
            }
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwspectate", "Spectates the match", false).Create(e ->
                e.withAliases("spectate")
                        .executesPlayer((p, args) -> {
                            SetSpectator(p);
                        }));
    }
}
