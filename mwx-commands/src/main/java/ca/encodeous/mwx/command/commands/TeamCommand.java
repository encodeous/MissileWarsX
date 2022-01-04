package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class TeamCommand extends MissileWarsCommand {
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

    public PlayerTeam GetTeam(String name){
       return switch (name){
            case "red" -> PlayerTeam.Red;
            case "green" -> PlayerTeam.Green;
            case "spectator" -> PlayerTeam.Spectator;
            case "lobby" -> PlayerTeam.None;
            default -> null;
        };
    }

    private void CompleteTeamSwitch(MissileWarsMatch sourceMatch, Collection<Entity> entities, PlayerTeam team){
        for(Entity entity : entities){
            if(entity instanceof Player pl){
                MissileWarsMatch match2 = LobbyEngine.FromPlayer(pl);
                if(sourceMatch != match2) continue;
                match2.AddPlayerToTeam(pl, team);
            }
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwteam", "Switches between teams", true).Create(e ->
                e.withAliases("mwt")
                        .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.MANY_PLAYERS),
                                new StringArgument("team").replaceSuggestions(r->new String[]{"red", "green", "lobby", "spectator"}))
                        .executesPlayer((sender, args) -> {
                            MissileWarsMatch match = LobbyEngine.FromPlayer(sender);
                            PlayerTeam team = GetTeam((String) args[1]);
                            if(team == null) CommandAPI.fail("Invalid team type");
                            CompleteTeamSwitch(match, (Collection<Entity>) args[0], team);
                        })
                        .executesCommandBlock((sender, args) -> {
                            MissileWarsMatch match = LobbyEngine.FromWorld(sender.getBlock().getWorld());
                            PlayerTeam team = GetTeam((String) args[1]);
                            if(team == null) CommandAPI.fail("Invalid team type");
                            CompleteTeamSwitch(match, (Collection<Entity>) args[0], team);
                        }));
    }
}
