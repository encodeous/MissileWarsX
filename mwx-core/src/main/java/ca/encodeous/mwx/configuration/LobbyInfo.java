package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyInfo implements ConfigurationSerializable {
    public int MaxTeamSize = 6;
    public boolean AutoJoin = true;
    public boolean IsRanked = false;
    public static LobbyInfo deserialize(Map<String, Object> args) {
        LobbyInfo conf = new LobbyInfo();
        conf.AutoJoin = (Boolean) args.get("use-auto-join");
        conf.MaxTeamSize = (Integer) args.get("max-players-per-team");
        conf.IsRanked = (Boolean) args.get("ranked-lobby");
        return conf;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("use-auto-join", AutoJoin);
        map.put("max-players-per-team", MaxTeamSize);
        map.put("ranked-lobby", IsRanked);
        return map;
    }
}
