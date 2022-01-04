package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WipeCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player p)
                WipeMap(p);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void WipeMap(Player p){
        var match = LobbyEngine.FromPlayer(p);
        if(match == null){
            p.sendMessage(Strings.LOBBY_COMMAND);
            return;
        }
        var lobby = match.lobby;
        if(lobby == null){
            p.sendMessage(Strings.LOBBY_COMMAND);
            return;
        }
        if(!Utils.CheckPrivPermission(p)) return;
        if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
            p.sendMessage("The map cannot be wiped at this time!");
            return;
        }
        for(Player pl : match.lobby.GetPlayers()){
            CoreGame.GetImpl().SendTitle(pl, "&9The map is being wiped", "&9by " + p.getDisplayName()+"&r.");
        }
        Lobby finalLobby = lobby;
        match.Wipe(()->{
            finalLobby.SendMessage("&9The lobby has been wiped");
        });
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwwipe", "Cleans the map", false).Create(e ->
                e.withAliases("wipe", "clearmap")
                        .withPermission(core.GetMwxAdminPermission())
                        .executesPlayer((p, args) -> {
                            WipeMap(p);
                        }));
    }
}
