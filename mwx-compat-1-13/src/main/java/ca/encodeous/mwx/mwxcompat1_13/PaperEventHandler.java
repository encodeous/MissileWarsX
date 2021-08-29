package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceType;
import ca.encodeous.mwx.mwxcore.missiletrace.TrackedBlock;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxcore.utils.TPSMon;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.UUID;

public class PaperEventHandler implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityAddToWorldEvent(EntityAddToWorldEvent event){
        if(!(event.getEntity() instanceof TNTPrimed)) return;
        MissileWarsMatch match = LobbyEngine.FromWorld(event.getEntity().getWorld());
        if(match == null) return;
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
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
    }

    public void InterceptTntIgnition(HashSet<UUID> sources, Block block, TNTPrimed tnt, boolean redstoneActivated, MissileWarsMatch match){
        TrackedBlock trace = match.Tracer.GetSources(block);
        if(trace == null) return;
        sources.addAll(trace.Sources);
        match.Tracer.RemoveBlock(trace.Position);
        match.Tracer.AddEntity(tnt, sources, redstoneActivated);
    }
}
