package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class MissileWarsConfiguration implements ConfigurationSerializable {
    /**
     * Number of seconds between Resupplies
     */
    public int ResupplySeconds = 15;
    /**
     * Number of seconds to check for draws for
     */
    public int DrawSeconds = 5;
    /**
     * Number of entities allowed in a lobby
     */
    public int HardEntityLimit = 1000;
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
    /**
     * Allow faster breaking on specific blocks like Redstone and Pistons
     */
    public boolean AllowFastBreak = false;
    /**
     * Should a stationary fireball explode if it is overlaps a block?
     */
    public boolean StationaryFireballExplode = true;
    /**
     * Configures individual block breaking speeds
     */
    public Map<String, Integer> BreakSpeeds;
    /**
     * A list of the allowed entities permitted to exist in the game, leave blank to allow any
     */
    public ArrayList<String> AllowedEntities;
    public static MissileWarsConfiguration deserialize(Map<String, Object> args) {
        MissileWarsConfiguration conf = new MissileWarsConfiguration();
        conf.Items = new ArrayList<>();
        ((List<Object>) args.get("items")).stream().map(x->(MissileWarsItem)x).forEach(x->conf.Items.add(x));
        conf.ResupplySeconds = (Integer)args.get("resupply-seconds");
        conf.DrawSeconds = (Integer)args.get("draw-seconds");
        conf.TpsWarningThreshold = (Integer)args.get("tps-warning-threshold");
        conf.TpsCriticalThreshold = (Integer)args.get("tps-critical-threshold");
        conf.HardEntityLimit = (Integer)args.get("lobby-entity-limit");
        conf.UseHelmets = (Boolean)args.get("use-helmets");
        conf.AllowFastBreak = (Boolean)args.get("use-fast-break");
        conf.BreakSpeeds = (Map<String, Integer>)args.get("break-speeds");
        conf.StationaryFireballExplode = (Boolean)args.get("stationary-fireball-explode");
        conf.AllowedEntities = (ArrayList<String>)args.get("allowed-entities");
        return conf;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("resupply-seconds", ResupplySeconds);
        map.put("draw-seconds", DrawSeconds);
        map.put("tps-warning-threshold", TpsWarningThreshold);
        map.put("lobby-entity-limit", HardEntityLimit);
        map.put("tps-critical-threshold", TpsCriticalThreshold);
        map.put("use-helmets", UseHelmets);
        map.put("items", Items);
        map.put("use-fast-break", AllowFastBreak);
        map.put("break-speeds", BreakSpeeds);
        map.put("stationary-fireball-explode", StationaryFireballExplode);
        map.put("allowed-entities", AllowedEntities);
        return map;
    }
}
