package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommands extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                if(args.length == 0){
                    SendPlayerBack((Player) sender);
                }else if(args.length == 1){
                    try{
                        int val = Integer.parseInt(args[0]);
                        SendPlayerTo((Player) sender, val);
                    }catch (NumberFormatException e){
                        return false;
                    }
                }else return false;

            }
            else{
                sender.sendMessage("You are not a player...");
            }
            return true;
        }catch (Exception e){

        }
        return false;
    }

    private void SendPlayerBack(Player sender){
        MissileWarsMatch match = LobbyEngine.FromPlayer((sender));
        match.RemovePlayer(sender);
        match.AddPlayerToTeam(sender, PlayerTeam.None);
    }

    public static void SendPlayerTo(Player sender, int val){
        if(val <= 0 || val > LobbyEngine.Lobbies.size()){
            sender.sendMessage("The lobby you specified does not exist.");
        }else{
            LobbyEngine.FromPlayer((Player) sender).lobby.RemovePlayer((Player) sender);
            LobbyEngine.GetLobby(val).AddPlayer((Player) sender);
            sender.sendMessage(Chat.FCL("&9You have been teleported to lobby " + val + "."));
        }
    }



    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("lobby", "Switches between lobbies", false).Create(e ->
                e.withAliases("mw", "mwlobby")
                        .withArguments(new IntegerArgument("lobbyId", 1, CoreGame.Instance.mwLobbies.Lobbies.size()))
                        .executesPlayer((p, args) -> {
                            SendPlayerTo(p, (Integer)args[0]);
                        })
        );
        new CommandRegister("leave", "Leaves the current lobby", false).Create(e ->
                e.withAliases("l", "mwleave")
                        .executesPlayer((p, args) -> {
                            SendPlayerBack(p);
                        }
        ));
    }
}
