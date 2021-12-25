package ca.encodeous.mwx.configuration;

import ca.encodeous.mwx.data.MissileMaterial;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class MissileBlock implements ConfigurationSerializable {
    public MissileMaterial Material;
    public PistonBlock PistonData;
    public Vector Location;
    public static BlockFace Rotate(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.SOUTH;

            case SOUTH:
                return BlockFace.NORTH;

            case EAST:
                return BlockFace.WEST;

            case WEST:
                return BlockFace.EAST;

            case UP:
                return BlockFace.UP;

            case DOWN:
                return BlockFace.DOWN;
        }

        return BlockFace.SELF;
    }
    public MissileBlock Flip(){
        MissileBlock block = new MissileBlock();
        if(Material == MissileMaterial.PISTON){
            block.PistonData = new PistonBlock();
            block.PistonData.Face = Rotate(PistonData.Face);
            block.PistonData.IsPowered = PistonData.IsPowered;
            block.PistonData.IsHead = PistonData.IsHead;
            block.PistonData.IsSticky = PistonData.IsSticky;
        }
        block.Material = Material;
        block.Location = new Vector(-Location.getBlockX(), Location.getBlockY(), -Location.getBlockZ());
        return block;
    }
    public static MissileBlock deserialize(Map<String, Object> args) {
        MissileBlock block = new MissileBlock();
        block.Material = MissileMaterial.valueOf((String)args.get("mat"));
        if(block.Material == MissileMaterial.PISTON){
            block.PistonData = (PistonBlock) args.get("piston-data");
        }
        block.Location = (Vector) args.get("rel-loc");
        return block;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("mat", this.Material.toString());
        if(Material == MissileMaterial.PISTON){
            o.put("piston-data", this.PistonData);
        }
        o.put("rel-loc", Location);
        return o;
    }
}
