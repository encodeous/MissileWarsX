package ca.encodeous.mwx.lobbyengine;

import ca.encodeous.mwx.configuration.LobbyInfo;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.lang.Strings;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import ca.encodeous.mwx.mwxstats.PlayerStats;
import com.keenant.tabbed.skin.SkinFetcher;
import de.gesundkrank.jskills.Rating;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kacperduras.protocoltab.manager.PacketTablist;
import pl.kacperduras.protocoltab.manager.TabItem;

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
        try {
            PlayerStats stat = CoreGame.Stats.statsDao.queryForId(p.getUniqueId());
            tabs.put(3, CreateText("&6Wins: &a" + stat.Wins));
            tabs.put(4, CreateText("&6Draws: &a" + stat.Draws));
            tabs.put(5, CreateText("&6Losses: &a" + stat.Losses));
            tabs.put(6, CreateText("&6Streak: &a" + stat.Streak));
            tabs.put(7, CreateText("&6All-time Streak: &a" + stat.MaxStreak));
            Rating rating = new Rating(stat.TrueSkill, stat.TrueSkillDev);
            tabs.put(8, CreateText("&6Trueskill: &a" + Chat.F(rating.getConservativeRating()) + ", " + Chat.F(rating.getStandardDeviation())));
            tabs.put(9, CreateText("&6Kills: &a" + stat.Kills));
            tabs.put(10, CreateText("&6Deaths: &a" + stat.Deaths));
            tabs.put(11, CreateText("&6Portals Broken: &a" + stat.PortalsBroken));
        } catch (Exception e) {
            tabs.put(3, CreateText("&cFailed fetching stats"));
            e.printStackTrace();
        }
        tabs.put(12, CreateText("&6Latency: &a" + Utils.GetPlayerPing(p)+" &ams"));

        // lobbies
        int lobbyCount = CoreGame.Instance.mwLobbies.Lobbies.size();
        if(lobbyCount > 17){
            tabs.put(39, CreateText("&7" + (lobbyCount - 17) + " more lobbies..."));
            int lIdx = 0;
            for(LobbyInfo lobby : CoreGame.Instance.mwLobbies.Lobbies){
                tabs.put(22 + lIdx, CreateText(Strings.MISSILE_WARS_BRAND + " &6" + lIdx));
                Lobby realLobby = GetLobby(lIdx);
                tabs.put(42 + lIdx, CreateText("&7" + lobby.MaxTeamSize + "v" + lobby.MaxTeamSize
                        + " (" + (lobby.AutoJoin ? "Auto": "Manual") + (lobby.IsRanked ? "Ranked": "Unranked") +") &f - &6" + realLobby.GetPlayers().size() + "&f/&7"
                        + (2 * lobby.MaxTeamSize)));
                if(lIdx == 16) break;
                lIdx++;
            }
        }else{
            int lIdx = 0;
            for(LobbyInfo lobby : CoreGame.Instance.mwLobbies.Lobbies){
                tabs.put(22 + lIdx, CreateText(Strings.MISSILE_WARS_BRAND + " &6" + lIdx));
                Lobby realLobby = GetLobby(lIdx);
                tabs.put(42 + lIdx, CreateText("&7" + lobby.MaxTeamSize + "v" + lobby.MaxTeamSize
                        + " (" + (lobby.AutoJoin ? "A": "M") + "/" + (lobby.IsRanked ? "R": "U") +") &f - &6" + realLobby.GetPlayers().size() + "&f/&7"
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
            tabs.put(79, CreateText(String.format(Strings.MORE_PLAYERS, players.size() - 17)));
        }
        for(int i = 0; i < Math.min(pLim, players.size()); i++){
            tabs.put(i + 62, CreatePlayer(players.get(i)));
        }
        for(int i = 0; i < 80; i++){
            if(!tabs.containsKey(i)){
                tabs.put(i, new TabItem(10000, " "));
            }
        }
        list.setHeader(Strings.TABLIST_HEADER);
        list.setFooter(Strings.TABLIST_FOOTER);
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
    public static Lobby CreateLobby(int teamSize, boolean isAutoJoin, boolean isRanked){
        int id = lobbyCount++;
        Lobby lobby = new Lobby(isAutoJoin, teamSize, id, isRanked);
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
