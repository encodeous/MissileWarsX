package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class LobbyConfiguration implements ConfigurationSerializable {
    public String Header = "&f&lWelcome to &c&lMissile&f&lWars&6&lX&f&l.";
    public String Footer = "&fUse &6/lobby &fto navigate between lobbies, and &6/players &fto see all players.\n" +
            "&e&oHelp contribute to MissileWarsX at &6https://github.com/encodeous/MissileWarsX&e!";
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
        LobbyInfo oneOnOne2 = new LobbyInfo();
        oneOnOne2.AutoJoin = true;
        oneOnOne2.MaxTeamSize = 1;
        return Arrays.asList(mainLobby, secondaryLobby, oneOnOne1, oneOnOne2);
    }
    public static LobbyConfiguration deserialize(Map<String, Object> args) {
        LobbyConfiguration conf = new LobbyConfiguration();
        conf.Lobbies = new ArrayList<>();
        ((List<Object>) args.get("lobbies")).stream().map(x->(LobbyInfo)x).forEach(x->conf.Lobbies.add(x));
        conf.Header = (String) args.get("header-text");
        conf.Footer = (String) args.get("footer-text");
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lobbies", Lobbies);
        map.put("header-text", Header);
        map.put("footer-text", Footer);
        return map;
    }
}
