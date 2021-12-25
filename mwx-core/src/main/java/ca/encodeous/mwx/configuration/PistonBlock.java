package ca.encodeous.mwx.configuration;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class PistonBlock implements ConfigurationSerializable {
    public boolean IsHead;
    public boolean IsSticky;
    public BlockFace Face;
    public boolean IsPowered;

    public static PistonBlock deserialize(Map<String, Object> args) {
        PistonBlock block = new PistonBlock();
        block.Face = BlockFace.valueOf((String)args.get("face"));
        block.IsPowered = (Boolean) args.get("power");
        block.IsHead = (Boolean) args.get("head");
        block.IsSticky = (Boolean) args.get("sticky");
        return block;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("face", Face.toString());
        o.put("power", IsPowered);
        o.put("head", IsHead);
        o.put("sticky", IsSticky);
        return o;
    }
}
