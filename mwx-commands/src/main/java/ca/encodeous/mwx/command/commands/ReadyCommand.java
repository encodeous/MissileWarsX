package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReadyCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(!(sender instanceof Player)){
                sender.sendMessage("You are not a player!");
                return true;
            }
            Player p = (Player) sender;
            RunReady(p);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void RunReady(Player p){
        Lobby lobby = null;
        if(LobbyEngine.FromPlayer(p) != null){
            lobby = LobbyEngine.FromPlayer(p).lobby;
        }
        if(lobby == null){
            p.sendMessage(Strings.LOBBY_COMMAND);
            return;
        }
        MissileWarsMatch match = lobby.Match;
        if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning() || match.hasStarted){
            p.sendMessage("You cannot run that command at this time");
            return;
        }
        if(!(match instanceof MissileWarsRankedMatch)){
            p.sendMessage("This command can only be run in a ranked game!");
            return;
        }
        if(!match.Red.contains(p) && !match.Green.contains(p)){
            p.sendMessage("You must be in a team to run this command");
            return;
        }
        MissileWarsRankedMatch rnk = (MissileWarsRankedMatch) match;
        if(match.Red.contains(p)){
            rnk.TeamReady(PlayerTeam.Red);
        }else{
            rnk.TeamReady(PlayerTeam.Green);
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("ready", "Marks your team as ready in a ranked match", false).Create(e ->
                        e.withAliases("mwready")
                                .executesPlayer((p, args) -> {
                            RunReady(p);
                        }));
    }
}
