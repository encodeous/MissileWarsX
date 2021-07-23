package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.mwxcompat1_13.nms.NMSCraftPlayer;
import ca.encodeous.mwx.mwxcompat1_13.nms.NMSCraftTNTPrimed;
import ca.encodeous.mwx.mwxcompat1_13.nms.NMSEntityLiving;
import ca.encodeous.mwx.mwxcompat1_13.nms.NMSEntityTNTPrimed;
import ca.encodeous.mwx.mwxcompat1_13.nms.nms_1_17.*;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.missiletrace.TrackedBlock;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.Bukkit;
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
import java.util.Random;
import java.util.UUID;

public class PaperEventHandler implements Listener {
    private Random rand = new Random();
    @EventHandler(priority = EventPriority.HIGHEST)
    public void TntPrimeEvent(TNTPrimeEvent e){
        if(e.getBlock().getX() <= -72){
            e.setCancelled(true);
            return;
        }
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
            InterceptTntIgnition(sources, latestSource, e.getBlock(), false, true);
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.ITEM){
            sources.add(e.getPrimerEntity().getUniqueId());
            InterceptTntIgnition(sources, e.getPrimerEntity().getUniqueId(), e.getBlock(), false, false);
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.PROJECTILE){
            ProjectileSource shooter = TraceEngine.ResolveShooter((Projectile) e.getPrimerEntity());
            UUID latestSource = null;
            if(shooter instanceof Player){
                latestSource = ((Player) shooter).getUniqueId();
                sources.add(latestSource);
            }
            if(e.getPrimerEntity() instanceof Fireball){
                InterceptTntIgnition(sources, latestSource, e.getBlock(), true, false);
            }else{
                InterceptTntIgnition(sources, latestSource, e.getBlock(), false, false);
            }
        }else if(e.getReason() == TNTPrimeEvent.PrimeReason.EXPLOSION){
            UUID latestSource = null;
            boolean isRedstoneActivated = false;
            if(e.getPrimerEntity() instanceof TNTPrimed){
                sources.addAll(CoreGame.Instance.mwMatch.Tracer.FindCause((TNTPrimed)e.getPrimerEntity()));
                latestSource = CoreGame.Instance.mwMatch.Tracer.FindRootCause((TNTPrimed)e.getPrimerEntity());
                isRedstoneActivated = CoreGame.Instance.mwMatch.Tracer.IsRedstoneActivated((TNTPrimed)e.getPrimerEntity());
            }
            InterceptTntIgnition(sources, latestSource, e.getBlock(), true, isRedstoneActivated);
        }
    }

    public void InterceptTntIgnition(HashSet<UUID> sources, UUID latestSource,  Block block, boolean isExplosion, boolean redstoneActivated){
        TrackedBlock trace = CoreGame.Instance.mwMatch.Tracer.GetSources(block);
        if(trace == null) return;
        sources.addAll(trace.Sources);
        CoreGame.Instance.mwMatch.Tracer.RemoveBlock(trace.Position);
        block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class, tnt->{
            if(isExplosion){
                tnt.setFuseTicks(rand.nextInt(20) + 10);
            }else{
                tnt.setFuseTicks(80);
            }
            if(latestSource != null) SetTntSource(tnt, Bukkit.getPlayer(latestSource));
            CoreGame.Instance.mwMatch.Tracer.AddEntity(tnt, sources, redstoneActivated);
        });
    }

    public void SetTntSource(TNTPrimed tnt, Player p) {
        if(p == null) return;
        if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_17.getValue()){
            NMSCraftTNTPrimed_1_17 craftTnt = NMSCore.getNMSObject(NMSCraftTNTPrimed_1_17.class, tnt);
            craftTnt.setSource(p);
        }
        else{
            NMSEntityLiving player = NMSCore.getNMSObject(NMSEntityLiving.class, ((NMSCore.getNMSObject(NMSCraftPlayer.class, p)).getHandle()).getProxyHandle());
            NMSEntityTNTPrimed tntPrimed = NMSCore.getNMSObject(NMSEntityTNTPrimed.class, NMSCore.getNMSObject(NMSCraftTNTPrimed.class,tnt).getHandle().getProxyHandle());
            tntPrimed.source(player);
        }
    }
}
