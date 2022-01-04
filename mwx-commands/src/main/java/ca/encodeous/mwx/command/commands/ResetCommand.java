package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player p){
                ResetMap(p);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void ResetMap(Player p){
        Lobby lobby = null;
        if(LobbyEngine.FromPlayer(p) != null){
            lobby = LobbyEngine.FromPlayer(p).lobby;
        }
        if(lobby == null){
            p.sendMessage(Strings.LOBBY_COMMAND);
            return;
        }
        MissileWarsMatch match = lobby.Match;
        if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
            p.sendMessage("The game cannot be reset at this time!");
            return;
        }
        match.EndGame();
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwreset", "Resets the map", false).Create(e ->
                e.withPermission(core.GetMwxAdminPermission())
                        .executesPlayer((p, args) -> {
                            ResetMap(p);
                        }));
    }
}
