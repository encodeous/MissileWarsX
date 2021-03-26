package ca.encodeous.mwx.structure;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.ItemConfig;
import ca.encodeous.mwx.config.MapConfig;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Utility methods for structure (missile / shield) placement validation and launching.
 *
 * <p>The protected-region constants below define the hard boundaries of the play area:
 * <ul>
 *   <li>X ≤ −72          – western boundary</li>
 *   <li>Y ≤ 0            – below ground</li>
 *   <li>Z ≥ +120 / ≤ −120 – north/south portal end-zones</li>
 *   <li>Y ≥ 150          – above sky limit</li>
 * </ul>
 */
public class StructureUtils {

    // ── Spawn-validation ────────────────────────────────────────────────────────────────────

    /**
     * Check whether a structure may be spawned at the given set of block positions.
     *
     * <p>For shields every block must pass the precondition check.
     * For missiles the bounding-box threshold algorithm (ported from OpenMissileWars) is used.
     *
     * @param team    the deploying player's team
     * @param blocks  all non-air block positions the structure would occupy
     * @param world   the game world
     * @param isShield true for shield schematics, false for missiles
     * @return true if the structure is allowed to spawn
     */
    public static boolean checkCanSpawn(PlayerTeam team, ArrayList<Vector> blocks,
                                        World world, boolean isShield, MapConfig cfg) {
        if (isShield) {
            for (Vector vec : blocks) {
                if (checkSpawnPreconditions(cfg, world, vec)) return false;
            }
            return true;
        }

        // Referenced from OpenMissileWars — bounding-box threshold check
        int boundingBoxThreshold = 0;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (Vector vec : blocks) {
            minX = Math.min(minX, vec.getBlockX()); maxX = Math.max(maxX, vec.getBlockX());
            minY = Math.min(minY, vec.getBlockY()); maxY = Math.max(maxY, vec.getBlockY());
            minZ = Math.min(minZ, vec.getBlockZ()); maxZ = Math.max(maxZ, vec.getBlockZ());
        }

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = minZ; k <= maxZ; k++) {
                    Vector vec = new Vector(i, j, k);
                    Block block = world.getBlockAt(i, j, k);

                    if (checkSpawnPreconditions(cfg, world, vec)) return false;

                    // crossMid: true when this block position has crossed into the enemy team's half.
                    // Red team occupies Z < 0, green team occupies Z > 0 (midpoint is Z = 0).
                    boolean crossMid = (team == PlayerTeam.RED) ? vec.getBlockZ() > 0
                                                                 : vec.getBlockZ() < 0;

                    boolean isSameTeamBlock = isBlockOfTeam(team, block);
                    boolean isNeutralBlock  = isNeutralBlock(block);
                    boolean isEnemyBlock    = !isSameTeamBlock && !isNeutralBlock && isGlassBlock(block);

                    if (isEnemyBlock && crossMid)  boundingBoxThreshold--;
                    if (isSameTeamBlock)            boundingBoxThreshold++;
                    if (isNeutralBlock && !crossMid) boundingBoxThreshold++;
                }
            }
        }
        return boundingBoxThreshold <= 4;
    }

    /**
     * Returns true when a spawn should be blocked at {@code vec}
     * (hard-blocked material or protected region).
     */
    private static boolean checkSpawnPreconditions(MapConfig cfg, World world, Vector vec) {
        Block block = world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
        Material mat = block.getType();
        if (mat == Material.OBSIDIAN || mat == Material.BEDROCK
                || mat == Material.NETHER_PORTAL
                || mat == Material.BARRIER) return true;
        return isInProtectedRegion(cfg, vec);
    }

    /** Returns true when {@code vec} lies within a hard-protected region of the map. */
    public static boolean isInProtectedRegion(MapConfig cfg, Vector vec) {
        return !cfg.boundingBox.isInBounds(vec);
    }

    // ── Block-classification helpers ────────────────────────────────────────────────────────

    /** Returns true when the block belongs to the given team's stained-glass colour. */
    public static boolean isBlockOfTeam(PlayerTeam team, Block block) {
        Material mat = block.getType();
        return switch (team) {
            case GREEN -> mat == Material.GREEN_STAINED_GLASS || mat == Material.LIME_STAINED_GLASS;
            case RED   -> mat == Material.RED_STAINED_GLASS   || mat == Material.PINK_STAINED_GLASS;
            case NONE  -> mat == Material.WHITE_STAINED_GLASS;
            default    -> false;
        };
    }

    /** Returns true when the block is the neutral/uncoloured (white) stained-glass placeholder. */
    public static boolean isNeutralBlock(Block block) {
        return block.getType() == Material.WHITE_STAINED_GLASS;
    }

    /** Returns true when the block is any variety of stained glass. */
    public static boolean isGlassBlock(Block block) {
        Material mat = block.getType();
        return mat == Material.WHITE_STAINED_GLASS
                || mat == Material.RED_STAINED_GLASS  || mat == Material.PINK_STAINED_GLASS
                || mat == Material.GREEN_STAINED_GLASS || mat == Material.LIME_STAINED_GLASS;
    }

    // ── Internal helpers ────────────────────────────────────────────────────────────────────

    /**
     * Build the list of world positions the given template would occupy when placed at
     * {@code origin}.
     */
    public static ArrayList<Vector> getTemplateBlocks(StructureTemplate template,
                                                      Vector origin, PlayerTeam team) {
        ArrayList<Vector> blocks = new ArrayList<>();
        Clipboard clipboard = template.clipboard;
        BlockVector3 clipOrigin = clipboard.getOrigin();

        for (BlockVector3 pt : clipboard.getRegion()) {
            BlockData data = BukkitAdapter.adapt(clipboard.getBlock(pt));
            if (data.getMaterial().isAir()) continue;

            int rx = pt.x() - clipOrigin.x();
            int ry = pt.y() - clipOrigin.y();
            int rz = pt.z() - clipOrigin.z();
            if (team == PlayerTeam.GREEN) rz = -rz;

            blocks.add(new Vector(
                    origin.getBlockX() + rx,
                    origin.getBlockY() + ry,
                    origin.getBlockZ() + rz));
        }
        return blocks;
    }
}
