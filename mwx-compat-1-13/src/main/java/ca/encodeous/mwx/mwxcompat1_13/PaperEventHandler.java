package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.missiletrace.TrackedBlock;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class PaperEventHandler implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityAddToWorldEvent(EntityAddToWorldEvent event){
        if(!(event.getEntity() instanceof TNTPrimed)) return;
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        Location blockLoc = tnt.getOrigin();
        Block b = tnt.getWorld().getBlockAt(blockLoc);
        HashSet<UUID> sources = new HashSet<>();
        if(tnt.getSource() == null){
            for(Block block : TraceEngine.GetNeighbors(b)){
                if(block.getType() == Material.REDSTONE_BLOCK){
                    TrackedBlock blockt = CoreGame.GetMatch().Tracer.GetSources(block);
                    sources.addAll(blockt.Sources);
                }
            }
            InterceptTntIgnition(sources, b, tnt, true);
        }else{
            if(tnt.getSource() instanceof Projectile){
                ProjectileSource shooter = TraceEngine.ResolveShooter((Projectile) tnt.getSource());
                UUID latestSource = null;
                if(shooter instanceof Player){
                    latestSource = ((Player) shooter).getUniqueId();
                    sources.add(latestSource);
                }
                InterceptTntIgnition(sources, b, tnt, false);
            }else if(tnt.getSource() instanceof TNTPrimed){
                boolean isRedstoneActivated = false;
                if(tnt.getSource() instanceof TNTPrimed){
                    sources.addAll(CoreGame.GetMatch().Tracer.FindCause((TNTPrimed)tnt.getSource()));
                    isRedstoneActivated = CoreGame.GetMatch().Tracer.IsRedstoneActivated((TNTPrimed)tnt.getSource());
                }
                InterceptTntIgnition(sources, b, tnt, isRedstoneActivated);
            }else{
                sources.add(tnt.getSource().getUniqueId());
                InterceptTntIgnition(sources, b, tnt, false);
            }
        }
    }

    public void InterceptTntIgnition(HashSet<UUID> sources, Block block, TNTPrimed tnt, boolean redstoneActivated){
        TrackedBlock trace = CoreGame.GetMatch().Tracer.GetSources(block);
        if(trace == null) return;
        sources.addAll(trace.Sources);
        CoreGame.GetMatch().Tracer.RemoveBlock(trace.Position);
        CoreGame.GetMatch().Tracer.AddEntity(tnt, sources, redstoneActivated);
    }
}
