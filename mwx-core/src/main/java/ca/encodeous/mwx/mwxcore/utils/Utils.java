package ca.encodeous.mwx.mwxcore.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Utils {
    public static Location LocationFromVec(Vector vec, World w){
        return new Location(w, vec.getX(), vec.getY(), vec.getZ());
    }
    public static Vector GetRandomNormalizedVector(){
        Vector v = Vector.getRandom();
        v.setX(v.getX() - 0.5f);
        v.setZ(v.getZ() - 0.5f);
        v.setY(v.getY() - 0.5f);
        return v.normalize();
    }
}
