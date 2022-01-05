package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class players extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("players", "online", "p", "glist", "globallist", "mwlist").Executes((context) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Strings.PLAYERS_ONLINE, Bukkit.getOnlinePlayers().size()));
            for(Lobby lobby : LobbyEngine.Lobbies.values()){
                sb.append(Strings.MISSILE_WARS_BRAND).append(" &6").append(lobby.lobbyId).append(": ").append(Chat.FormatPlayerlist(new ArrayList<>(lobby.GetPlayers()))).append("\n");
            }
            context.SendMessage(sb.toString());
            return Bukkit.getOnlinePlayers().size();
        });
    }

    @Override
    public String GetCommandName() {
        return "players";
    }
}
