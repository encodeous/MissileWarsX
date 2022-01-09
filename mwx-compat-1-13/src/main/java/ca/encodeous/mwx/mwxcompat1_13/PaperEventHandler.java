package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.trace.TraceEngine;
import ca.encodeous.mwx.engines.trace.TrackedBlock;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaperEventHandler implements Listener {
    private static ConcurrentHashMap<World, Integer> entityCount = new ConcurrentHashMap<>();
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityRemoveFromWorldEvent(EntityRemoveFromWorldEvent event){
        entityCount.put(event.getEntity().getWorld(), entityCount.getOrDefault(event.getEntity().getWorld(), 1) - 1);
        MissileWarsMatch match = LobbyEngine.FromWorld(event.getEntity().getWorld());
        if(match == null) return;
        match.Tracer.RemoveEntity(event.getEntity().getUniqueId());
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityAddToWorldEvent(EntityAddToWorldEvent event){
        if(CoreGame.Instance.mwConfig.AllowedEntities != null && !CoreGame.Instance.mwConfig.AllowedEntities.isEmpty()){
            if(!CoreGame.Instance.mwConfig.AllowedEntities.contains(event.getEntity().getType().name())){
                Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, ()->{
                    event.getEntity().remove();
                }, 1);
                return;
            }
        }
        entityCount.put(event.getEntity().getWorld(), entityCount.getOrDefault(event.getEntity().getWorld(), 0) + 1);
        if(entityCount.getOrDefault(event.getEntity().getWorld(), 0) > CoreGame.Instance.mwConfig.HardEntityLimit){
            if(!(event.getEntity() instanceof Player)){
                Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, ()->{
                    event.getEntity().remove();
                }, 1);
                return;
            }
        }
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
