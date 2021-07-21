package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import org.bukkit.ChatColor;

public class Formatter {
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
}
