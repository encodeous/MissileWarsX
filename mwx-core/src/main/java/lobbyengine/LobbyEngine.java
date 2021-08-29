package lobbyengine;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kacperduras.protocoltab.manager.PacketTablist;
import pl.kacperduras.protocoltab.manager.TabItem;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyEngine {
    private static int worldCount = 0;
    public static ConcurrentHashMap<UUID, Lobby> Lobbies = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, UUID> LobbyNames = new ConcurrentHashMap<>();
    public static ArrayList<MissileWarsMap> MapCache = new ArrayList<>();
    public static void BuildTablist(Player p, PacketTablist list){
        Map<Integer, TabItem> tabs = list.getMutableSlots();
        for (int i = 0; i < 80; i++) {
            tabs.put(i, new TabItem(10000, "Item - " + i + " ---- "));
        }
        list.setHeader("&cHEADER TEST");
        list.setFooter("&a&lDEVELOPMENT SERVER\n&f&lNEW LINE");
    }
    public static int AllocateWorldId(){
        return worldCount++;
    }
    public static Lobby GetLobby(String name){
        if(!LobbyNames.containsKey(name)) return null;
        return Lobbies.get(LobbyNames.get(name));
    }
    public static void EnsureCache(boolean isAutoJoin, int count){
        int cnt = 0;
        for(MissileWarsMap curMap : MapCache){
            if(curMap.SeparateJoin != isAutoJoin){
                cnt++;
            }
        }
        for(int i = 0; i < count - cnt; i++){
            if(isAutoJoin){
                MapCache.add(CoreGame.GetImpl().CreateManualJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId()));
            }else{
                MapCache.add(CoreGame.GetImpl().CreateAutoJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId()));
            }
        }
    }
    public static boolean IsCached(boolean isAutoJoin){
        int cnt = 0;
        for(MissileWarsMap curMap : MapCache){
            if(curMap.SeparateJoin != isAutoJoin){
                cnt++;
            }
        }
        return cnt > 0;
    }
    public static MissileWarsMatch FromPlayer(Player p){
        for(Lobby lobby : Lobbies.values()){
            if(lobby.Match.Teams.containsKey(p)){
                return lobby.Match;
            }
        }
        return null;
    }
    public static Lobby CreateLobby(String name, int teamSize, boolean isTemporary, boolean isAutoJoin){
        if(GetLobby(name) != null){
            throw new RuntimeException("The lobby already exists!");
        }
        UUID id = UUID.randomUUID();
        Lobby lobby = new Lobby(isAutoJoin, !isTemporary, teamSize, name, id);
        MissileWarsMap map = null;
        for(MissileWarsMap curMap : MapCache){
            if(curMap.SeparateJoin != isAutoJoin){
                map = curMap;
                MapCache.remove(curMap);
                break;
            }
        }
        if(map == null){
            if(isAutoJoin){
                map = CoreGame.GetImpl().CreateManualJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId());
            }else{
                map = CoreGame.GetImpl().CreateAutoJoinMap("mwx_match_" + LobbyEngine.AllocateWorldId());
            }
        }
        lobby.Match.Map = map;
        Lobbies.put(id, lobby);
        LobbyNames.put(name, id);
        return lobby;
    }
    public static void DeleteLobby(UUID id, boolean recycle){
        if(!Lobbies.containsKey(id)) return;
        Lobby lobby = Lobbies.get(id);
        LobbyNames.remove(lobby.lobbyName);
        if(!lobby.isClosed){
            lobby.CloseLobby(GetLobby("default"), recycle);
            if(recycle){
                MapCache.add(lobby.Match.Map);
            }
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
            DeleteLobby(lobby.lobbyId, false);
        }
        for(MissileWarsMap map : MapCache){
            for(Player p : map.MswWorld.getPlayers()){
                p.kickPlayer("Resetting Map");
            }
            boolean firstTry = Bukkit.unloadWorld(map.MswWorld.getName(), false);
            boolean success = firstTry;
            if(!firstTry){
                for(Player p : map.MswWorld.getPlayers()){
                    p.kickPlayer("Resetting Map");
                }
                success = Bukkit.unloadWorld(map.MswWorld.getName(), false);
            }
            if(!success){
                System.out.println("Unable to unload world " + map.MswWorld.getName() + " deleting anyways...");
            }
            try {
                FileUtils.deleteDirectory(map.MswWorld.getWorldFolder());
            } catch (IOException e) {
                try {
                    FileUtils.forceDeleteOnExit(map.MswWorld.getWorldFolder());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
