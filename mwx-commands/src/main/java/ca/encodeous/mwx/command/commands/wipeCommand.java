package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Utils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class wipeCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Lobby lobby = null;
            if(sender instanceof Player){
                if(LobbyEngine.FromPlayer((Player) sender) != null){
                    lobby = LobbyEngine.FromPlayer((Player) sender).lobby;
                }
            }
            Player cur = (Player) sender;
            if(lobby == null){
                sender.sendMessage(Strings.LOBBY_COMMAND);
                return true;
            }
            if(!Utils.CheckPrivPermission(cur)) return true;
            MissileWarsMatch match = lobby.Match;
            if(match.Map.isBusy || match.endCounter.isRunning() || match.startCounter.isRunning()){
                sender.sendMessage("The map cannot be wiped at this time!");
                return true;
            }
            for(Player p : match.lobby.GetPlayers()){
                CoreGame.GetImpl().SendTitle(p, "&9The map is being wiped", "&9by " + cur.getDisplayName()+"&r.");
            }
            Lobby finalLobby = lobby;
            match.Wipe(()->{
                finalLobby.SendMessage("&9The lobby has been wiped");
            });
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder) {

    }

    @Override
    public String GetCommandName() {
        return "wipe";
    }
}
