package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import lobbyengine.Lobby;
import lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class playersCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("&2Currently there are " + Bukkit.getOnlinePlayers().size() + " player(s) online.\n");
        for(Lobby lobby : LobbyEngine.Lobbies.values()){
            sb.append("&c&lMissile&f&lWars &6" + lobby.lobbyId + ": " + Chat.FormatPlayerlist(new ArrayList<>(lobby.GetPlayers())) + "\n");
        }
        sender.sendMessage(Chat.FCL(sb.toString()));
        return true;
    }
}
