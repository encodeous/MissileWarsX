package ca.encodeous.mwx.structure;

import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.game.MwMatch;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Places structures (missiles, shields, …) by reading blocks from a FAWE clipboard and writing
 * them to the game world via the standard Bukkit API — no NMS required.
 *
 * <p>Placement follows the same two-pass strategy as the vanilla {@code /clone} command:
 * <ol>
 *   <li>Set every destination block without triggering physics.</li>
 *   <li>Apply physics for every placed block so pistons and redstone activate.</li>
 * </ol>
 *
 * <p>Team colour mapping is applied at placement time:
 * {@code WHITE_STAINED_GLASS} → team-coloured glass,
 * {@code WHITE_TERRACOTTA} → team-coloured terracotta.
 */
public class StructurePlacer {

    /** Simple record holding destination position and the Bukkit BlockData to place. */
    private record BlockCopy(int x, int y, int z, BlockData data) {}

    /**
     * Place a structure by iterating the schematic clipboard and writing blocks to the
     * target location.
     *
     * @param template  the structure template (holds the loaded clipboard)
     * @param target    placement origin in the game world
     * @param team      color of the team. (forward +Z for red), (mirror: forward −Z for green)
     * @param player    player deploying the structure (for explosion attribution, may be null)
     * @param match     active game match
     * @return true if at least one block was placed
     */
    public static boolean placeStructure(StructureTemplate template, Location target,
                                         PlayerTeam team, Player player, MwMatch match) {
        var isRed = team == PlayerTeam.RED;
        World gameWorld = target.getWorld();
        if (gameWorld == null) return false;

        Clipboard clipboard = template.clipboard;
        BlockVector3 origin = clipboard.getOrigin();

        int tx = target.getBlockX();
        int ty = target.getBlockY();
        int tz = target.getBlockZ();

        Deque<BlockCopy> copies = new ArrayDeque<>();

        if (!isRed) {
            clipboard = clipboard.transform(
                    new AffineTransform()
                            .rotateY(180));
        }

        // same as /clone
        for (int z = clipboard.getMinimumPoint().z(); z <= clipboard.getMaximumPoint().z(); z++) {
            for (int y = clipboard.getMinimumPoint().y(); y <= clipboard.getMaximumPoint().y(); y++) {
                for (int x = clipboard.getMinimumPoint().x(); x <= clipboard.getMaximumPoint().x(); x++) {
                    var pt = BlockVector3.at(x, y, z);

                    BlockState weState = clipboard.getBlock(pt);
                    BlockData data = BukkitAdapter.adapt(weState);

                    int rx = x - origin.x();
                    int ry = y - origin.y();
                    int rz = z - origin.z();

                    if (data.getMaterial().isAir()) {
                        copies.addFirst(new BlockCopy(tx + rx, ty + ry, tz + rz, gameWorld.getBlockData(tx + rx, ty + ry, tz + rz)));
                        continue;
                    }

                    if(template.id.equals("shield")) {
                        data = applyTeamColourShield(data, isRed);
                    } else {
                        data = applyTeamColour(data, isRed);
                    }

                    copies.addLast(new BlockCopy(tx + rx, ty + ry, tz + rz, data));
                }
            }
        }

        if (copies.isEmpty()) return false;

        for (BlockCopy copy : copies.reversed()) {
            Block block = gameWorld.getBlockAt(copy.x(), copy.y(), copy.z());
            block.setType(Material.BARRIER, true);
        }

        for (BlockCopy copy : copies) {
            Block block = gameWorld.getBlockAt(copy.x(), copy.y(), copy.z());
            block.setType(copy.data.getMaterial(), true);
            block.setBlockData(copy.data(), false);

            // Register TNT / redstone blocks for explosion attribution
            if (player != null) {
                Material mat = copy.data().getMaterial();
                if (mat == Material.TNT || mat == Material.REDSTONE_BLOCK) {
                    match.getTracer().trackBlock(
                            new Vector(copy.x(), copy.y(), copy.z()),
                            player.getUniqueId());
                }
            }
        }

        for (BlockCopy copy : copies.reversed()) {
            var mat = copy.data.getMaterial();
            if(mat != Material.SLIME_BLOCK && mat != Material.REDSTONE_BLOCK) continue;
            Block block = gameWorld.getBlockAt(copy.x(), copy.y(), copy.z());
            block.setType(Material.BARRIER, true);
            block.setType(copy.data.getMaterial(), true);
            block.setBlockData(copy.data(), true);
        }
        return true;
    }

    private static BlockData applyTeamColour(BlockData data, boolean isRed) {
        return switch (data.getMaterial()) {
            case QUARTZ_BLOCK ->
                    Bukkit.createBlockData(isRed ? Material.RED_TERRACOTTA : Material.GREEN_TERRACOTTA);
            case WHITE_STAINED_GLASS ->
                    Bukkit.createBlockData(isRed ? Material.RED_STAINED_GLASS : Material.GREEN_STAINED_GLASS);
            case WHITE_TERRACOTTA ->
                    Bukkit.createBlockData(isRed ? Material.RED_TERRACOTTA : Material.GREEN_TERRACOTTA);
            default -> data;
        };
    }

    // need to specially handle shields since they have white stained glass in the palette
    private static BlockData applyTeamColourShield(BlockData data, boolean isRed) {
        return switch (data.getMaterial()) {
            case TINTED_GLASS ->
                    Bukkit.createBlockData(isRed ? Material.RED_STAINED_GLASS : Material.GREEN_STAINED_GLASS);
            case GLASS ->
                    Bukkit.createBlockData(isRed ? Material.PINK_STAINED_GLASS : Material.LIME_STAINED_GLASS);
            default -> data;
        };
    }
}
