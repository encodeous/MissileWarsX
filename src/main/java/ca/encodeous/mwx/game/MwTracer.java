package ca.encodeous.mwx.game;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Tracks which players placed which blocks/entities for explosion attribution.
 * Used to credit portal destruction to the correct players.
 */
public class MwTracer {

    public static class TrackedBlock {
        public Vector pos;
        public Set<UUID> sources;

        public TrackedBlock(Vector pos, Collection<UUID> sources) {
            this.pos = pos;
            this.sources = new HashSet<>(sources);
        }
    }

    public static class TrackedEntity {
        public UUID entityId;
        public Set<UUID> sources;

        public TrackedEntity(UUID entityId, Collection<UUID> sources) {
            this.entityId = entityId;
            this.sources = new HashSet<>(sources);
        }
    }

    private final HashMap<Vector, TrackedBlock> blocks = new HashMap<>();
    private final HashMap<UUID, TrackedEntity> entities = new HashMap<>();

    public void trackBlock(Vector pos, UUID... sources) {
        var trk = blocks.getOrDefault(pos, new TrackedBlock(pos, Collections.emptySet()));
        trk.sources.addAll(List.of(sources));
        blocks.put(pos, trk);
    }

    public Set<UUID> lookupBlock(Vector pos) {
        if (blocks.containsKey(pos)) {
            return blocks.get(pos).sources;
        } else {
            return Collections.emptySet();
        }
    }

    public void trackEntity(Entity entity, UUID... sources) {
        var trk = entities.getOrDefault(entity.getUniqueId(), new TrackedEntity(entity.getUniqueId(), Collections.emptySet()));
        trk.sources.addAll(List.of(sources));
        entities.put(entity.getUniqueId(), trk);
    }

    public Set<UUID> lookupEntity(Entity entity) {
        if (entity instanceof Player) {
            return Set.of(entity.getUniqueId());
        }
        if (entities.containsKey(entity.getUniqueId())) {
            return entities.get(entity.getUniqueId()).sources;
        } else {
            return Collections.emptySet();
        }
    }

    public void untrackBlock(Vector pos) {
        blocks.remove(pos);
    }

    public void updatePistonPush(List<Block> pushed, BlockFace direction) {
        List<Map.Entry<Vector, TrackedBlock>> toUpdate = new ArrayList<>();
        for (Block b : pushed) {
            Vector pos = b.getLocation().toVector();
            TrackedBlock tracked = blocks.get(pos);
            if (tracked != null) {
                toUpdate.add(Map.entry(pos, tracked));
            }
        }
        for (Map.Entry<Vector, TrackedBlock> entry : toUpdate) {
            blocks.remove(entry.getKey());
            var newPos = entry.getKey().add(direction.getDirection());
            entry.getValue().pos = newPos;
            blocks.put(newPos, entry.getValue());
        }
    }

    public static Set<Block> propagatePortalBreak(Block startBlock) {
        Set<Block> result = new HashSet<>();
        if (startBlock.getType() != org.bukkit.Material.NETHER_PORTAL) return result;

        Queue<Block> queue = new LinkedList<>();
        queue.add(startBlock);
        result.add(startBlock);

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN};

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (!result.contains(neighbor) && neighbor.getType() == org.bukkit.Material.NETHER_PORTAL) {
                    result.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }

    public void clear() {
        blocks.clear();
        entities.clear();
    }
}
