package lobbyengine;

import ca.encodeous.mwx.configuration.LobbyInfo;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import com.keenant.tabbed.skin.SkinFetcher;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kacperduras.protocoltab.manager.PacketTablist;
import pl.kacperduras.protocoltab.manager.TabItem;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyEngine {
    public static ConcurrentHashMap<Integer, Lobby> Lobbies = new ConcurrentHashMap<>();
    private static int lobbyCount, worldCount = 0;
    public static SkinFetcher Fetcher;
    public static void BuildTablist(Player p, PacketTablist list){
        Map<Integer, TabItem> tabs = list.getMutableSlots();

        tabs.clear();

        MissileWarsMatch match = FromPlayer(p);

        if(match == null) return;

        tabs.put(1, CreateText("&7&lYOUR INFO"));
        tabs.put(21, CreateText("&7&lLOBBIES"));
        tabs.put(41, CreateText("&7&lLOBBY INFO"));
        tabs.put(61, CreateText("&7&lYOUR GAME"));

        // player info
        tabs.put(2, CreateText("&6Current Lobby: &a" + match.lobby.lobbyId));
        tabs.put(3, CreateText("&6Wins: &aN/A"));
        tabs.put(4, CreateText("&6Games: &aN/A"));
        tabs.put(5, CreateText("&6Streak: &aN/A"));
        tabs.put(6, CreateText("&6All-time Streak: &aN/A"));
        tabs.put(7, CreateText("&6Trueskill: &aN/A"));
        tabs.put(8, CreateText("&6K/D Ratio: &aN/A"));
        tabs.put(9, CreateText("&6Time Played: &aN/A"));
        tabs.put(10, CreateText("&6Latency: &a" + Utils.GetPlayerPing(p)+" &ams"));

        // lobbies
        int lobbyCount = CoreGame.Instance.mwLobbies.Lobbies.size();
        if(lobbyCount > 17){
            tabs.put(39, CreateText("&7" + (lobbyCount - 17) + " more lobbies..."));
            int lIdx = 0;
            for(LobbyInfo lobby : CoreGame.Instance.mwLobbies.Lobbies){
                tabs.put(22 + lIdx, CreateText("&c&lMissile&f&lWars &6" + lIdx));
                Lobby realLobby = GetLobby(lIdx);
                tabs.put(42 + lIdx, CreateText("&7" + lobby.MaxTeamSize + "v" + lobby.MaxTeamSize
                        + " &f - &6" + realLobby.GetPlayers().size() + "&f/&7" + (2 * lobby.MaxTeamSize)));
                if(lIdx == 16) break;
                lIdx++;
            }
        }else{
            int lIdx = 0;
            for(LobbyInfo lobby : CoreGame.Instance.mwLobbies.Lobbies){
                tabs.put(22 + lIdx, CreateText("&c&lMissile&f&lWars &6" + lIdx));
                Lobby realLobby = GetLobby(lIdx);
                tabs.put(42 + lIdx, CreateText("&7" + lobby.MaxTeamSize + "v" + lobby.MaxTeamSize
                        + " (" + (lobby.AutoJoin ? "A": "M") + ") &f - &6" + realLobby.GetPlayers().size() + "&f/&7"
                        + (2 * lobby.MaxTeamSize)));
                lIdx++;
            }
        }

        // players
        ArrayList<Player> players = new ArrayList<>();
        players.addAll(match.Green);
        players.addAll(match.Red);
        players.addAll(match.Spectators);
        players.addAll(match.None);
        int pLim = 17;
        if(players.size() > 17){
            pLim = 16;
            tabs.put(79, CreateText("&7" + (players.size() - 17) + " more players..."));
        }
        for(int i = 0; i < Math.min(pLim, players.size()); i++){
            tabs.put(i + 62, CreatePlayer(players.get(i)));
        }
        for(int i = 0; i < 80; i++){
            if(!tabs.containsKey(i)){
                tabs.put(i, new TabItem(10000, " "));
            }
        }
        list.setHeader(CoreGame.Instance.mwLobbies.Header);
        list.setFooter(CoreGame.Instance.mwLobbies.Footer);
    }
    private static TabItem CreateText(String text){
        return new TabItem(10000, text);
    }
    private static TabItem CreatePlayer(Player p){
        return new TabItem(Utils.GetPlayerPing(p), p.getDisplayName(), Fetcher.getPlayer(p));
    }

    public static int AllocateWorldId(){
        return worldCount++;
    }
    public static Lobby GetLobby(Integer lobby){
        if(!Lobbies.containsKey(lobby)) return null;
        return Lobbies.get(lobby);
    }
    public static MissileWarsMatch FromPlayer(Player p){
        for(Lobby lobby : Lobbies.values()){
            if(lobby.Match.Teams.containsKey(p)){
                return lobby.Match;
            }
        }
        return null;
    }
    public static Lobby CreateLobby(int teamSize, boolean isAutoJoin){
        int id = lobbyCount++;
        Lobby lobby = new Lobby(isAutoJoin, teamSize, id);
        lobby.Match.Map = CreateMap(isAutoJoin);;
        lobby.Match.Map.CreateMap(()->{});
        Lobbies.put(id, lobby);
        return lobby;
    }
    public static MissileWarsMap CreateMap(boolean isAutoJoin){
        MissileWarsMap map;
        if(!isAutoJoin){
            map = CoreGame.GetImpl().CreateManualJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId());
        }else{
            map = CoreGame.GetImpl().CreateAutoJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId());
        }
        return map;
    }
    public static void DeleteLobby(int id){
        if(!Lobbies.containsKey(id)) return;
        Lobby lobby = Lobbies.get(id);
        if(!lobby.isClosed){
            lobby.CloseLobby(GetLobby(0));
        }
        Lobbies.remove(id);
    }
    public static MissileWarsMatch FromWorld(World w){
        for(Lobby lobby : Lobbies.values()){
            if(lobby.Match.Map.MswWorld == w){
                return lobby.Match;
            }
        }
        return null;
    }

    public static void Shutdown(){
        for(Lobby lobby : Lobbies.values()){
            DeleteLobby(lobby.lobbyId);
        }
        Lobbies = new ConcurrentHashMap<>();
        lobbyCount = 0;
        worldCount = 0;
    }
}
