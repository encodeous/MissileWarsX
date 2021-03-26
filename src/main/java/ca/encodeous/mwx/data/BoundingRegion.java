package ca.encodeous.mwx.data;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * An axis-aligned bounding region used for portal detection, map bounds, and region checks.
 */
public class BoundingRegion {

    public int minX, minY, minZ;
    public int maxX, maxY, maxZ;

    public BoundingRegion(Vector min, Vector max) {
        this.minX = Math.min(min.getBlockX(), max.getBlockX());
        this.minY = Math.min(min.getBlockY(), max.getBlockY());
        this.minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        this.maxX = Math.max(min.getBlockX(), max.getBlockX());
        this.maxY = Math.max(min.getBlockY(), max.getBlockY());
        this.maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
    }

    public static BoundingRegion of(Vector a, Vector b) {
        return new BoundingRegion(a, b);
    }

    public boolean isInBounds(Vector v) {
        int x = v.getBlockX(), y = v.getBlockY(), z = v.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean isInVerticalBounds(Vector v) {
        int y = v.getBlockY();
        return y >= minY && y <= maxY;
    }

    /**
     * Expand this region to include the given point.
     */
    public void stretch(Vector v) {
        int x = v.getBlockX(), y = v.getBlockY(), z = v.getBlockZ();
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (z < minZ) minZ = z;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
        if (z > maxZ) maxZ = z;
    }

    /**
     * Clamp a location to the top of this region (maxY), keeping X/Z.
     */
    public Location clampToTop(Location loc, World world) {
        return new Location(world, loc.getX(), maxY, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Parse two "x,y,z" strings into a BoundingRegion.
     */
    public static BoundingRegion fromConfig(String minStr, String maxStr) {
        return new BoundingRegion(parseVec(minStr), parseVec(maxStr));
    }

    /**
     * Parse a two-element config list into a BoundingRegion.
     */
    public static BoundingRegion fromConfig(List<String> corners) {
        if (corners == null || corners.size() != 2) {
            throw new IllegalArgumentException("BoundingRegion requires exactly 2 corner values");
        }
        return fromConfig(corners.get(0), corners.get(1));
    }

    private static Vector parseVec(String s) {
        String[] parts = s.trim().split(",");
        return new Vector(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
        );
    }

    public BlockVector3 getMinPoint() {
        return BlockVector3.at(minX, minY, minZ);
    }

    public BlockVector3 getMaxPoint() {
        return BlockVector3.at(maxX, maxY, maxZ);
    }

    /**
     * Convert this region to a WorldEdit CuboidRegion for FAWE operations.
     */
    public CuboidRegion toWorldEditRegion(World world) {
        com.sk89q.worldedit.world.World weWorld =
                com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world);
        return new CuboidRegion(weWorld, getMinPoint(), getMaxPoint());
    }

    @Override
    public String toString() {
        return "BoundingRegion[(" + minX + "," + minY + "," + minZ + ")-("
                + maxX + "," + maxY + "," + maxZ + ")]";
    }
}
