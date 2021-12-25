package ca.encodeous.mwx.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class MissileConfiguration implements ConfigurationSerializable {
    /**
     * Missile Wars Item id of the missile
     */
    public String MissileItemId;
    /**
     * Schematic of the missile;
     */
    public MissileSchematic Schematic;

    public static MissileConfiguration deserialize(Map<String, Object> args) {
        MissileConfiguration missile = new MissileConfiguration();
        missile.Schematic = (MissileSchematic) args.get("schematic");
        missile.MissileItemId = (String) args.get("attached-item-id");
        return missile;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("attached-item-id", this.MissileItemId);
        o.put("schematic", this.Schematic);
        return o;
    }
}
