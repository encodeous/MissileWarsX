package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.trace.TraceEngine;
import ca.encodeous.mwx.engines.trace.TrackedBlock;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaperEventHandler implements Listener {
    public static ConcurrentHashMap<World, HashSet<Entity>> entities = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<World, Integer> lzEntityQuery = new ConcurrentHashMap<>();
    private void LzSqrtProcess(World w){
        if(lzEntityQuery.getOrDefault(w, 0) > 30){
            lzEntityQuery.put(w, 0);
            if(!entities.containsKey(w)) return;
            ArrayList<Entity> removed = new ArrayList<>();
            for(var e : entities.get(w)){
                if(e.isDead()){
                    removed.add(e);
                }
            }
            removed.forEach(entities.get(w)::remove);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityRemoveFromWorldEvent(EntityDeathEvent event){
        if(entities.containsKey(event.getEntity().getWorld())){
            lzEntityQuery.put(event.getEntity().getWorld(), lzEntityQuery.getOrDefault(event.getEntity().getWorld(), 0) + 1);
            entities.get(event.getEntity().getWorld()).remove(event.getEntity());
            LzSqrtProcess(event.getEntity().getWorld());
            MissileWarsMatch match = LobbyEngine.FromWorld(event.getEntity().getWorld());
            if(match == null) return;
            match.Tracer.RemoveEntity(event.getEntity().getUniqueId());
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityAddEvent(EntitySpawnEvent event){
        LzSqrtProcess(event.getEntity().getWorld());
        if(CoreGame.Instance.mwConfig.AllowedEntities != null && !CoreGame.Instance.mwConfig.AllowedEntities.isEmpty()){
            lzEntityQuery.put(event.getEntity().getWorld(), lzEntityQuery.getOrDefault(event.getEntity().getWorld(), 0) + 1);
            if(!CoreGame.Instance.mwConfig.AllowedEntities.contains(event.getEntity().getType().name())){
                event.setCancelled(true);
                return;
            }
        }
        var w = event.getEntity().getWorld();
        if(!entities.containsKey(w)){
            entities.put(w, new HashSet<>());
        }
        var wEnt = entities.get(w);
        lzEntityQuery.put(event.getEntity().getWorld(), lzEntityQuery.getOrDefault(event.getEntity().getWorld(), 0) + 1);
        if(wEnt.size() > CoreGame.Instance.mwConfig.HardEntityLimit){
            if(!(event.getEntity() instanceof Player)){
                event.setCancelled(true);
                return;
            }
        }
        wEnt.add(event.getEntity());
        if(!(event.getEntity() instanceof TNTPrimed)) return;
        MissileWarsMatch match = LobbyEngine.FromWorld(event.getEntity().getWorld());
        if(match == null) return;
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        try{
            Location blockLoc = tnt.getOrigin();
            Block b = tnt.getWorld().getBlockAt(blockLoc);
            HashSet<UUID> sources = new HashSet<>();
            if(tnt.getSource() == null){
                for(Block block : TraceEngine.GetNeighbors(b)){
                    if(block.getType() == Material.REDSTONE_BLOCK){
                        TrackedBlock blockt = match.Tracer.GetSources(block);
                        sources.addAll(blockt.Sources);
                    }
                }
                InterceptTntIgnition(sources, b, tnt, true, match);
            }else{
                if(tnt.getSource() instanceof Projectile){
                    ProjectileSource shooter = TraceEngine.ResolveShooter((Projectile) tnt.getSource());
                    UUID latestSource = null;
                    if(shooter instanceof Player){
                        latestSource = ((Player) shooter).getUniqueId();
                        sources.add(latestSource);
                    }
                    InterceptTntIgnition(sources, b, tnt, false, match);
                }else if(tnt.getSource() instanceof TNTPrimed){
                    boolean isRedstoneActivated = false;
                    if(tnt.getSource() instanceof TNTPrimed){
                        sources.addAll(match.Tracer.FindCause((TNTPrimed)tnt.getSource()));
                        isRedstoneActivated = match.Tracer.IsRedstoneActivated((TNTPrimed)tnt.getSource());
                    }
                    InterceptTntIgnition(sources, b, tnt, isRedstoneActivated, match);
                }else{
                    sources.add(tnt.getSource().getUniqueId());
                    InterceptTntIgnition(sources, b, tnt, false, match);
                }
            }
        }catch (IllegalStateException e){
            // ignored
        }
    }

    public void InterceptTntIgnition(HashSet<UUID> sources, Block block, TNTPrimed tnt, boolean redstoneActivated, MissileWarsMatch match){
        TrackedBlock trace = match.Tracer.GetSources(block);
        if(trace == null) return;
        sources.addAll(trace.Sources);
        match.Tracer.RemoveBlock(trace.Position);
        match.Tracer.AddEntity(tnt, sources, redstoneActivated);
    }
}
