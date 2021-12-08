package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.lang.Strings;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.lobbyengine.Lobby;
import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class playersCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Strings.PLAYERS_ONLINE, Bukkit.getOnlinePlayers().size()));
        for(Lobby lobby : LobbyEngine.Lobbies.values()){
            sb.append(Strings.MISSILE_WARS_BRAND + " &6" + lobby.lobbyId + ": " + Chat.FormatPlayerlist(new ArrayList<>(lobby.GetPlayers())) + "\n");
        }
        sender.sendMessage(Chat.FCL(sb.toString()));
        return true;
    }
}
