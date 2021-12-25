package ca.encodeous.mwx.data;

import ca.encodeous.mwx.core.utils.Utils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Bounds implements ConfigurationSerializable {
    public Vector Min = null, Max = null;
    public static Bounds deserialize(Map<String, Object> data){
        Bounds bounds = new Bounds();
        bounds.Min = (Vector) data.get("low");
        bounds.Max = (Vector) data.get("high");
        return bounds;
    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("low", Min);
        data.put("high", Max);
        return data;
    }
    public static Bounds of(Vector a, Vector b){
        Bounds x = new Bounds();
        x.stretch(a);
        x.stretch(b);
        return x;
    }

    public CuboidRegion toWorldeditRegion(World world){
        return new CuboidRegion(
                BukkitAdapter.asBlockVector(Utils.LocationFromVec(Min, world)),
                BukkitAdapter.asBlockVector(Utils.LocationFromVec(Max, world))
        );
    }

    public boolean IsInBounds(Vector x){
        if(Min.getX() <= x.getX() && x.getX() <= Max.getX()){
            if(Min.getY() <= x.getY() && x.getY() <= Max.getY()){
                if(Min.getZ() <= x.getZ() && x.getZ() <= Max.getZ()){
                    return true;
                }
            }
        }
        return false;
    }

    public Bounds clone(){
        Bounds b = new Bounds();
        if(b.Max == null) return b;
        b.Min = Min.clone();
        b.Max = Max.clone();
        return b;
    }

    public Bounds stretch(Vector v){
        if(Min == null){
            Min = v.clone();
            Max = v.clone();
        }else{
            Min.setX(Math.min(Min.getBlockX(), v.getBlockX()));
            Min.setY(Math.min(Min.getBlockY(), v.getBlockY()));
            Min.setZ(Math.min(Min.getBlockZ(), v.getBlockZ()));

            Max.setX(Math.max(Max.getBlockX(), v.getBlockX()));
            Max.setY(Math.max(Max.getBlockY(), v.getBlockY()));
            Max.setZ(Math.max(Max.getBlockZ(), v.getBlockZ()));
        }
        return this;
    }

    public int getMinX() {
        return Min.getBlockX();
    }
    public int getMaxX() {
        return Max.getBlockX();
    }
    public int getMinY() {
        return Min.getBlockY();
    }
    public int getMaxY() {
        return Max.getBlockY();
    }
    public int getMinZ() {
        return Min.getBlockZ();
    }
    public int getMaxZ() {
        return Max.getBlockZ();
    }
}
