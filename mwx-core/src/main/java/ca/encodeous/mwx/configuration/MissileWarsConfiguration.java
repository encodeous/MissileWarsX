package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MissileWarsConfiguration implements ConfigurationSerializable {
    /**
     * Player Join Balancing Strategy
     */
    public BalanceStrategy Strategy = BalanceStrategy.BALANCED_RANDOM;
    /**
     * Maximum number of players allowed on a team
     */
    public int TeamPlayerCap = 6;
    /**
     * Number of seconds between Resupplies
     */
    public int ResupplySeconds = 15;
    /**
     * Registered Missile Wars Items
     */
    public ArrayList<MissileWarsItem> Items;
    /**
     * Should helmets be given to players?
     */
    public boolean UseHelmets = false;
    /**
     * Should the shield still spawn if it hits a block?
     */
    public boolean AllowShieldHit = false;

    public static MissileWarsConfiguration deserialize(Map<String, Object> args) {
        MissileWarsConfiguration conf = new MissileWarsConfiguration();
        conf.Items = new ArrayList<>();
        ((List<Object>) args.get("items")).stream().map(x->(MissileWarsItem)x).forEach(x->conf.Items.add(x));
        conf.Strategy = BalanceStrategy.valueOf((String)args.get("strategy"));
        conf.TeamPlayerCap = (Integer) args.get("player-cap");
        conf.ResupplySeconds = (Integer)args.get("resupply-seconds");
        conf.UseHelmets = (Boolean)args.get("use-helmets");
        conf.AllowShieldHit = (Boolean)args.get("allow-shield-spawn-after-block-hit");
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("strategy", Strategy.toString());
        map.put("player-cap", TeamPlayerCap);
        map.put("resupply-seconds", ResupplySeconds);
        map.put("use-helmets", UseHelmets);
        map.put("allow-shield-spawn-after-block-hit", AllowShieldHit);
        map.put("items", Items);
        return map;
    }
}
