package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.soundengine.SoundType;
import ca.encodeous.mwx.lobbyengine.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Chat {
    public static String FCL(String s){
        return ChatColor.translateAlternateColorCodes('&',s);
    }
    public static String ResolveTeamColor(PlayerTeam team){
        if (team == PlayerTeam.None) {
            return FCL("&7");
        }
        else if (team == PlayerTeam.Spectator) {
            return FCL("&9");
        }
        else if(team == PlayerTeam.Green){
            return FCL("&a");
        }
        else return FCL("&c");
    }
    public static String FormatPlayerAction(Player p, String action){
        return p.getDisplayName() + "&r " + action;
    }
    public static void TeamWin(ArrayList<Player> credits, Lobby lobby, PlayerTeam winningTeam, PlayerTeam losingTeam){
        if(!credits.isEmpty()){
            lobby.SendMessage("&fThe " + ResolveTeamColor(losingTeam) + losingTeam.name() + " &fteam's portal was blown up by " + FormatPlayerlist(credits) + "&r!");
        }else{
            lobby.SendMessage("&fThe " + ResolveTeamColor(losingTeam) + losingTeam.name() + " &fteam's portal was blown up!");
        }
        lobby.SendMessage("&6Congratulations " + ResolveTeamColor(winningTeam) + winningTeam.name() + " &6team!");
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().SendTitle(p, "&6The " + ResolveTeamColor(winningTeam) + winningTeam.name() + " &6team has won!", "&6Congratulations!");
        }
    }
    public static void TeamDraw(ArrayList<Player> credits, Lobby lobby, PlayerTeam losingTeam){
        if(!credits.isEmpty()){
            lobby.SendMessage("&fThe " + ResolveTeamColor(losingTeam) + losingTeam.name() + " &fteam's portal was blown up by " + FormatPlayerlist(credits) + "&r!");
        }else{
            lobby.SendMessage("&fThe " + ResolveTeamColor(losingTeam) + losingTeam.name() + " &fteam's portal was blown up!");
        }
        lobby.SendMessage("&6The game has ended in a draw!");
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().PlaySound(p, SoundType.WIN);
        }
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().SendTitle(p, "&6The game has ended in a draw!", "");
        }
    }
    public static String FormatPlayerlist(ArrayList<Player> credits){
        if(credits.size() == 0) return "";
        StringBuilder winString = new StringBuilder();
        if(credits.size() >= 2){
            for(int i = 0; i < credits.size() - 1; i++){
                if(i != 0) winString.append("&r, ");
                winString.append(credits.get(i).getDisplayName());
            }
            winString.append("&r and ");
            winString.append(credits.get(credits.size() - 1).getDisplayName());
        }else{
            winString.append(credits.get(0).getDisplayName());
        }
        return winString.toString();
    }
}
