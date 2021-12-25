package ca.encodeous.mwx.configuration;


import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All missiles default to the red team
 */
public class MissileSchematic implements ConfigurationSerializable {
    public ArrayList<MissileBlock> Blocks;
    public MissileSchematic CreateOppositeSchematic(){
        MissileSchematic schem = new MissileSchematic();
        schem.Blocks = new ArrayList<>();
        for(MissileBlock blc : Blocks){
            MissileBlock fblc = blc.Flip();
            schem.Blocks.add(fblc);
        }
        return schem;
    }
    public static MissileSchematic deserialize(Map<String, Object> args) {
        MissileSchematic schem = new MissileSchematic();
        schem.Blocks = new ArrayList<>();
        ((List<Object>) args.get("blocks")).stream().map(x->(MissileBlock)x).forEach(x->schem.Blocks.add(x));
        return schem;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("blocks", this.Blocks);
        return o;
    }
}
