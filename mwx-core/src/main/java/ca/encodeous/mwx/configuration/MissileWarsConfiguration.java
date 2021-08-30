package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MissileWarsConfiguration implements ConfigurationSerializable {
    /**
     * Number of seconds between Resupplies
     */
    public int ResupplySeconds = 15;
    /**
     * Tps threshold to warn players of potential lag
     */
    public int TpsWarningThreshold = 17;
    /**
     * Tps threshold to prevent intense lag from crashing the server
     */
    public int TpsCriticalThreshold = 10;
    /**
     * Registered Missile Wars Items
     */
    public ArrayList<MissileWarsItem> Items;
    /**
     * Should helmets be given to players?
     */
    public boolean UseHelmets = false;

    public static MissileWarsConfiguration deserialize(Map<String, Object> args) {
        MissileWarsConfiguration conf = new MissileWarsConfiguration();
        conf.Items = new ArrayList<>();
        ((List<Object>) args.get("items")).stream().map(x->(MissileWarsItem)x).forEach(x->conf.Items.add(x));
        conf.ResupplySeconds = (Integer)args.get("resupply-seconds");
        conf.TpsWarningThreshold = (Integer)args.get("tps-warning-threshold");
        conf.TpsCriticalThreshold = (Integer)args.get("tps-critical-threshold");
        conf.UseHelmets = (Boolean)args.get("use-helmets");
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("resupply-seconds", ResupplySeconds);
        map.put("tps-warning-threshold", TpsWarningThreshold);
        map.put("tps-critical-threshold", TpsCriticalThreshold);
        map.put("use-helmets", UseHelmets);
        map.put("items", Items);
        return map;
    }
}
