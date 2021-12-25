package ca.encodeous.mwx.core.utils;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.data.SoundType;
import ca.encodeous.mwx.engines.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
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
            lobby.SendMessage(String.format(Strings.WIN_GAME_CREDITED, ResolveTeamColor(losingTeam) + losingTeam.name(), FormatPlayerlist(credits)));
        }else{
            lobby.SendMessage(String.format(Strings.WIN_GAME, ResolveTeamColor(losingTeam) + losingTeam.name()));
        }
        lobby.SendMessage(String.format(Strings.CONGRATULATE_WIN, ResolveTeamColor(winningTeam) + winningTeam.name()));
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().SendTitle(p, String.format(Strings.CONGRATULATE_WIN_TITLE, ResolveTeamColor(winningTeam) + winningTeam.name()), "&6"+ Strings.CONGRATULATIONS +"!");
        }
    }
    private static DecimalFormat df = new DecimalFormat("###.##");
    public static String F(double d){
        return df.format(d);
    }
    public static void TeamDraw(ArrayList<Player> credits, Lobby lobby, PlayerTeam losingTeam){
        if(!credits.isEmpty()){
            lobby.SendMessage(String.format(Strings.WIN_GAME_CREDITED, ResolveTeamColor(losingTeam) + losingTeam.name(), FormatPlayerlist(credits)));
        }else{
            lobby.SendMessage(String.format(Strings.WIN_GAME, ResolveTeamColor(losingTeam) + losingTeam.name()));
        }
        lobby.SendMessage(Strings.GAME_DRAW);
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().PlaySound(p, SoundType.WIN);
        }
        for(Player p : lobby.Match.Teams.keySet()){
            CoreGame.GetImpl().SendTitle(p, Strings.GAME_DRAW, "");
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
            winString.append("&r "+Strings.AND+" ");
            winString.append(credits.get(credits.size() - 1).getDisplayName());
        }else{
            winString.append(credits.get(0).getDisplayName());
        }
        return winString.toString();
    }
}
