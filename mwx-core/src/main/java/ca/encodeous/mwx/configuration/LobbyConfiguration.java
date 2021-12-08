package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class LobbyConfiguration implements ConfigurationSerializable {
    public List<LobbyInfo> Lobbies = GetDefaultLobbyInfo();
    public static List<LobbyInfo> GetDefaultLobbyInfo(){
        LobbyInfo mainLobby = new LobbyInfo();
        mainLobby.AutoJoin = true;
        mainLobby.MaxTeamSize = 6;
        LobbyInfo secondaryLobby = new LobbyInfo();
        secondaryLobby.AutoJoin = false;
        secondaryLobby.MaxTeamSize = 6;
        LobbyInfo oneOnOne1 = new LobbyInfo();
        oneOnOne1.AutoJoin = true;
        oneOnOne1.MaxTeamSize = 1;
        oneOnOne1.IsRanked = true;
        LobbyInfo oneOnOne2 = new LobbyInfo();
        oneOnOne2.AutoJoin = true;
        oneOnOne2.IsRanked = true;
        oneOnOne2.MaxTeamSize = 1;
        return Arrays.asList(mainLobby, secondaryLobby, oneOnOne1, oneOnOne2);
    }
    public static LobbyConfiguration deserialize(Map<String, Object> args) {
        LobbyConfiguration conf = new LobbyConfiguration();
        conf.Lobbies = new ArrayList<>();
        ((List<Object>) args.get("lobbies")).stream().map(x->(LobbyInfo)x).forEach(x->conf.Lobbies.add(x));
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lobbies", Lobbies);
        return map;
    }
}
