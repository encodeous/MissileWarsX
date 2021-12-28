package ca.encodeous.mwx.engines.trace;

import ca.encodeous.mwx.data.TraceType;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.CoreGame;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class TraceEngine {
    private final HashMap<Vector, TrackedBlock> blocks = new HashMap<>();
    private final HashMap<Vector, TrackedBreakage> breaks = new HashMap<>();
    private final HashMap<UUID, TrackedEntity> entities = new HashMap<>();

    public void AddBlock(UUID source, TraceType type, Vector loc){
        TrackedBlock block = blocks.getOrDefault(loc, new TrackedBlock());
        block.Position = loc;
        block.Sources.add(source);
        block.Type = type;
        blocks.put(loc, block);
    }

    public TrackedBreakage AddBreak(Block b, int breakTimeMs){
        TrackedBreakage block = breaks.getOrDefault(b.getLocation().toVector(),
                new TrackedBreakage(b, breakTimeMs, LobbyEngine.FromWorld(b.getWorld()).lobby.Match));
        block.Position = b.getLocation().toVector();
        breaks.put(b.getLocation().toVector(), block);
        return block;
    }

    public TrackedBreakage GetBreak(Block b){
        return breaks.getOrDefault(b.getLocation().toVector(), null);
    }

    public TrackedEntity GetSources(Entity entity){
        return entities.getOrDefault(entity.getUniqueId(), new TrackedEntity());
    }
    public TrackedBlock GetSources(Block block){
        return blocks.getOrDefault(block.getLocation().toVector(), new TrackedBlock());
    }

    /**
     * Push the blocks in a direction and update the engine
     */
    public void TransformBlocks(List<Block> blocks, BlockFace face){
        ArrayList<Block> tracked = new ArrayList<>();
        ArrayList<TrackedBlock> blockMeta = new ArrayList<>();
        // blocks
        for(Block block : blocks){
            Vector v = block.getLocation().toVector();
            if(this.blocks.containsKey(v)){
                tracked.add(block);
                blockMeta.add(this.blocks.get(v));
                this.blocks.remove(v);
            }
        }
        for(int i = 0; i < tracked.size(); i++){
            Vector newPos = PushBlockPos(tracked.get(i).getLocation().toVector(), face);
            TrackedBlock block = this.blocks.getOrDefault(newPos, new TrackedBlock());
            MergeBlockMeta(block, blockMeta.get(i));
            this.blocks.put(newPos, block);
        }
        // fast breaks
        if(CoreGame.Instance.mwConfig.UseFastBreak){
            for(Block block : blocks){
                Vector v = block.getLocation().toVector();
                Vector newPos = PushBlockPos(v, face);
                breaks.remove(v);
                breaks.remove(newPos);
            }
        }
    }

    private void MergeBlockMeta(TrackedBlock a, TrackedBlock b){
        if(a == null) return;
        a.Sources.addAll(b.Sources);
        a.Type = b.Type;
    }

    private Vector PushBlockPos(Vector vec, BlockFace face){
        return vec.add(new Vector(face.getModX(), face.getModY(), face.getModZ()));
    }

    /**
     * Track an entity
     */
    public void AddEntity(Entity e, HashSet<UUID> sources, boolean isRedstoneActivated){
        TrackedEntity entity = entities.getOrDefault(e.getUniqueId(), new TrackedEntity());
        entity.EntityId = e.getUniqueId();
        entity.Sources.addAll(sources);
        entity.IsRedstoneActivated = isRedstoneActivated;
        entities.put(e.getUniqueId(), entity);
    }

    /**
     * Untrack an entity
     */
    public void RemoveEntity(UUID entity){
        entities.remove(entity);
    }

    /**
     * Find the cause of the tnt explosion
     */
    public HashSet<UUID> FindCause(TNTPrimed result){
        HashSet<UUID> causes = new HashSet<>();
        if(entities.containsKey(result.getUniqueId())){
            causes.addAll(entities.get(result.getUniqueId()).Sources);
        }
        if(result.getSource() instanceof TNTPrimed){
            causes.addAll(FindCause((TNTPrimed) result.getSource()));
        }else if(result.getSource() instanceof Projectile){
            ProjectileSource shooter = ResolveShooter((Projectile)result.getSource());
            if(shooter instanceof Player){
                causes.add(((Player) shooter).getUniqueId());
            }
        }else if(result.getSource() instanceof Player){
            causes.add(result.getSource().getUniqueId());
        }
        return causes;
    }

    /**
     * Find the root cause of the explosion
     */
//    public UUID FindRootCause(TNTPrimed result){
//        if(result.getSource() instanceof TNTPrimed){
//            return FindRootCause((TNTPrimed) result.getSource());
//        }else if(result.getSource() instanceof Projectile){
//            ProjectileSource shooter = ResolveShooter((Projectile)result.getSource());
//            if(shooter instanceof Player){
//                return ((Player) shooter).getUniqueId();
//            }
//        }else if(result.getSource() instanceof Player){
//            return result.getSource().getUniqueId();
//        }
//        return null;
//    }
    /**
     * Resolve the (root) shooter of a projectile
     */
    public static ProjectileSource ResolveShooter(Projectile projectile){
        if(projectile.getShooter() instanceof Projectile){
            return ResolveShooter((Projectile)projectile.getShooter());
        }else{
            return projectile.getShooter();
        }
    }

    /**
     * Untrack a block
     */
    public void RemoveBlock(Vector loc){
        blocks.remove(loc);
        breaks.remove(loc);
    }

    /**
     * Untrack a breakage
     */
    public void RemoveBreak(Vector loc){
        breaks.remove(loc);
    }

    final static int[] offsetx = new int[]{1,-1,0,0,0,0};
    final static int[] offsety = new int[]{0,0,1,-1,0,0};
    final static int[] offsetz = new int[]{0,0,0,0,1,-1};

    public static void PropagatePortalBreak(Block block){
        ArrayList<Block> blocks = new ArrayList<>();
        PropagatePortalBreakInternal(block, block.getWorld(), new HashSet<>(), blocks);
        for(Block rem : blocks){
            rem.setType(Material.AIR);
        }
    }

    public boolean IsRedstoneActivated(TNTPrimed tnt){
        if(entities.containsKey(tnt.getUniqueId())){
            return entities.get(tnt.getUniqueId()).IsRedstoneActivated;
        }
        return false;
    }

    public static ArrayList<Block> GetNeighbors(Block block){
        ArrayList<Block> blocks = new ArrayList<>();
        World world = block.getWorld();
        for(int i = 0; i < 6; i++){
            blocks.add(world.getBlockAt(block.getX() + offsetx[i], block.getY() + offsety[i], block.getZ() + offsetz[i]));
        }
        return blocks;
    }

    private static void PropagatePortalBreakInternal(Block block, World world, HashSet<Vector> visited, ArrayList<Block> remove){
        // Just a simple bfs :)
        Queue<Block> queue = new ArrayDeque<>();
        if(block.getType() == CoreGame.GetImpl().GetPortalMaterial()){
            remove.add(block);
            queue.add(block);
            visited.add(block.getLocation().toVector());
        }else{
            queue.add(block);
            visited.add(block.getLocation().toVector());
        }

        while(!queue.isEmpty()){
            Block cur = queue.poll();
            for(int i = 0; i < 6; i++){
                Block newBlock = world.getBlockAt(cur.getX() + offsetx[i], cur.getY() + offsety[i], cur.getZ() + offsetz[i]);
                if(visited.contains(newBlock.getLocation().toVector())) continue;
                visited.add(newBlock.getLocation().toVector());
                if(newBlock.getType() == CoreGame.GetImpl().GetPortalMaterial()){
                    remove.add(newBlock);
                    queue.add(newBlock);
                }
            }
        }
    }
}
