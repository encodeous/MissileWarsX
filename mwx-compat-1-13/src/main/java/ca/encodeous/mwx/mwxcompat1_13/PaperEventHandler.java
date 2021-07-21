package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.missiletrace.TrackedBlock;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
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
    public void TntPrimeEvent(TNTPrimeEvent e){
        if(e.getReason() == TNTPrimeEvent.PrimeReason.FIRE) e.setCancelled(true);
        HashSet<UUID> sources = new HashSet<>();
        e.getBlock().setType(Material.AIR, false);
        e.setCancelled(true);
        if(e.getReason() == TNTPrimeEvent.PrimeReason.REDSTONE){
            UUID latestSource = null;
            for(Block block : TraceEngine.GetNeighbors(e.getBlock())){
                if(block.getType() == Material.REDSTONE_BLOCK){
                    TrackedBlock blockt = CoreGame.Instance.mwMatch.Tracer.GetSources(block);
                    sources.addAll(blockt.Sources);
                    if(!blockt.Sources.isEmpty()) latestSource = blockt.Sources.stream().findAny().get();
                }
            }
            CoreGame.Instance.mwMatch.InterceptTntIgnition(sources, latestSource, e.getBlock(), false, true);
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.ITEM){
            sources.add(e.getPrimerEntity().getUniqueId());
            CoreGame.Instance.mwMatch.InterceptTntIgnition(sources, e.getPrimerEntity().getUniqueId(), e.getBlock(), false, false);
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.PROJECTILE){
            ProjectileSource shooter = TraceEngine.ResolveShooter((Projectile) e.getPrimerEntity());
            UUID latestSource = null;
            if(shooter instanceof Player){
                latestSource = ((Player) shooter).getUniqueId();
                sources.add(latestSource);
            }
            if(e.getPrimerEntity() instanceof Fireball){
                CoreGame.Instance.mwMatch.InterceptTntIgnition(sources, latestSource, e.getBlock(), true, false);
            }else{
                CoreGame.Instance.mwMatch.InterceptTntIgnition(sources, latestSource, e.getBlock(), false, false);
            }
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.EXPLOSION){
            UUID latestSource = null;
            boolean isRedstoneActivated = false;
            if(e.getPrimerEntity() instanceof TNTPrimed){
                sources.addAll(CoreGame.Instance.mwMatch.Tracer.FindCause((TNTPrimed)e.getPrimerEntity()));
                latestSource = CoreGame.Instance.mwMatch.Tracer.FindRootCause((TNTPrimed)e.getPrimerEntity());
                isRedstoneActivated = CoreGame.Instance.mwMatch.Tracer.IsRedstoneActivated((TNTPrimed)e.getPrimerEntity());
            }
            CoreGame.Instance.mwMatch.InterceptTntIgnition(sources, latestSource, e.getBlock(), true, isRedstoneActivated);
        }
    }
}
