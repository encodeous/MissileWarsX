package ca.encodeous.mwx.mwxcore.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Utils {
    public static Location LocationFromVec(Vector vec, World w){
        return new Location(w, vec.getX(), vec.getY(), vec.getZ());
    }
}
