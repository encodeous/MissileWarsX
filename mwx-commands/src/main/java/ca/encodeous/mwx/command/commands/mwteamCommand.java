package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mwteamCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Lobby lobby = null;
            if(sender instanceof Player){
                if(LobbyEngine.FromPlayer((Player) sender) != null){
                    lobby = LobbyEngine.FromPlayer((Player) sender).lobby;
                }
            }
            if(lobby == null){
                sender.sendMessage(Strings.LOBBY_COMMAND);
                return true;
            }
            MissileWarsMatch match = lobby.Match;
            if(match instanceof MissileWarsRankedMatch){
                sender.sendMessage("Cannot switch teams in a ranked game.");
                return true;
            }
            Player p = Bukkit.getPlayer(args[0]);
            if(p == null){
                sender.sendMessage("Player not found!");
                return true;
            }
            if(args[1].equals("green")){
                match.AddPlayerToTeam(p, PlayerTeam.Green);
            } else if(args[1].equals("red")){
                match.AddPlayerToTeam(p, PlayerTeam.Red);
            } else if(args[1].equals("spectator")){
                match.AddPlayerToTeam(p, PlayerTeam.Spectator);
            } else if(args[1].equals("lobby")){
                match.AddPlayerToTeam(p, PlayerTeam.None);
            }else{
                return false;
            }
            sender.sendMessage("Done!");
            return true;
        }catch (Exception e){

        }
        return false;
    }

    @Override
    public void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder) {

    }

    @Override
    public String GetCommandName() {
        return "mwteam";
    }
}
