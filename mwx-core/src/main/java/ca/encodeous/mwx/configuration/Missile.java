package ca.encodeous.mwx.configuration;

import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Missile implements ConfigurationSerializable {
    /**
     * Display name of the missile in kill messages
     */
    public String MissileDisplayName;
    /**
     * Missile Wars Item id of the missile
     */
    public String MissileItemId;
    /**
     * Schematic of the missile;
     */
    public MissileSchematic Schematic;

    public static Missile deserialize(Map<String, Object> args) {
        Missile missile = new Missile();
        missile.Schematic = (MissileSchematic) args.get("schematic");
        missile.MissileDisplayName = (String) args.get("display-name");
        missile.MissileItemId = (String) args.get("attached-item-id");
        return missile;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("attached-item-id", this.MissileItemId);
        o.put("display-name", this.MissileDisplayName);
        o.put("schematic", this.Schematic);
        return o;
    }
}
