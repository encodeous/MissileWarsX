package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ready extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            if(!(sender instanceof Player)){
                sender.sendMessage("You are not a player!");
                return true;
            }
            Player p = (Player) sender;
            Lobby lobby = null;
            if(LobbyEngine.FromPlayer(p) != null){
                lobby = LobbyEngine.FromPlayer(p).lobby;
            }
            if(lobby == null){
                sender.sendMessage(Strings.LOBBY_COMMAND);
                return true;
            }
            MissileWarsMatch match = lobby.Match;
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning() || match.hasStarted){
                sender.sendMessage("You cannot run that command at this time");
                return true;
            }
            if(!(match instanceof MissileWarsRankedMatch)){
                sender.sendMessage("This command can only be run in a ranked game!");
                return true;
            }
            if(!match.Red.contains(p) && !match.Green.contains(p)){
                sender.sendMessage("You must be in a team to run this command");
                return true;
            }
            MissileWarsRankedMatch rnk = (MissileWarsRankedMatch) match;
            if(match.Red.contains(p)){
                rnk.TeamReady(PlayerTeam.Red);
            }else{
                rnk.TeamReady(PlayerTeam.Green);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("ready").Executes(context -> {
            MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
            Lobby lobby = match != null ? match.lobby : null;
            if(lobby == null) {
                context.SendMessage(Strings.LOBBY_COMMAND);
                return 0;
            }
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning() || match.hasStarted){
                context.SendMessage("&cYou cannot run that command at this time");
                return 0;
            }
            if(!(match instanceof MissileWarsRankedMatch rankedMatch)){
                context.SendMessage("&cThis command can only be run in a ranked game!");
                return 0;
            }
            if(!match.Red.contains(context.GetPlayer()) && !match.Green.contains(context.GetPlayer())){
                context.SendMessage("&cYou must be in a team to run this command");
                return 0;
            }
            if(match.Red.contains(context.GetPlayer())){
                rankedMatch.TeamReady(PlayerTeam.Red);
            }else{
                rankedMatch.TeamReady(PlayerTeam.Green);
            }
            return 1;
        });
    }

    @Override
    public String GetCommandName() {
        return "ready";
    }
}
