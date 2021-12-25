package ca.encodeous.mwx.configuration;

import ca.encodeous.mwx.data.MatchType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class LobbyConfiguration implements ConfigurationSerializable {
    public List<LobbyDescription> Lobbies = GetDefaultLobbyInfo();
    public static List<LobbyDescription> GetDefaultLobbyInfo(){
        LobbyDescription mainLobby = new LobbyDescription();
        mainLobby.AutoJoin = true;
        mainLobby.MaxTeamSize = 6;
        LobbyDescription secondaryLobby = new LobbyDescription();
        secondaryLobby.AutoJoin = false;
        secondaryLobby.MaxTeamSize = 6;
        LobbyDescription oneOnOne1 = new LobbyDescription();
        oneOnOne1.AutoJoin = true;
        oneOnOne1.MaxTeamSize = 1;
        oneOnOne1.LobbyType = MatchType.RANKED;
        LobbyDescription oneOnOne2 = new LobbyDescription();
        oneOnOne2.AutoJoin = false;
        oneOnOne2.LobbyType = MatchType.PRACTICE;
        oneOnOne2.MaxTeamSize = 10;
        return Arrays.asList(mainLobby, secondaryLobby, oneOnOne1, oneOnOne2);
    }
    public static LobbyConfiguration deserialize(Map<String, Object> args) {
        LobbyConfiguration conf = new LobbyConfiguration();
        conf.Lobbies = new ArrayList<>();
        ((List<Object>) args.get("lobbies")).stream().map(x->(LobbyDescription)x).forEach(x->conf.Lobbies.add(x));
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lobbies", Lobbies);
        return map;
    }
}
