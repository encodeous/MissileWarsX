package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.engines.trace.TraceEngine;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.structure.StructureUtils;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;
import java.util.UUID;

import static ca.encodeous.mwx.engines.trace.TraceEngine.PropagatePortalBreak;

public class MissileWarsEventHandler extends ca.encodeous.mwx.mwxcompat1_8.MissileWarsEventHandler {
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e){
        Ref<Boolean> cancel = new Ref<>(false);
        Ref<Boolean> use = new Ref<>(false);
        MissileWarsMatch match = LobbyEngine.FromPlayer(e.getPlayer());
        if(match == null) return;
        match.EventHandler.PlayerInteractEvent(e.getPlayer(), e.getAction(), e.getClickedBlock(), e.getItem(), cancel, use);
        if(use.val){
            if(e.getHand() == EquipmentSlot.HAND){
                ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
                if(item.getAmount() == 1){
                    item.setType(Material.AIR);
                }else{
                    item.setAmount(item.getAmount() - 1);
                }
                e.getPlayer().getInventory().setItemInMainHand(item);
            }
            else{
                ItemStack item = e.getPlayer().getInventory().getItemInOffHand();
                if(item.getAmount() == 1){
                    item.setType(Material.AIR);
                }else{
                    item.setAmount(item.getAmount() - 1);
                }
                e.getPlayer().getInventory().setItemInOffHand(item);
            }
        }
        if(cancel.val){
            e.setCancelled(true);
        }
        Player p = (Player) e.getPlayer().getInventory().getHolder();
        ReequipGunblade(p);
    }
    @EventHandler
    public void PlayerCombatEvent(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof Player){
            MissileWarsMatch match = LobbyEngine.FromPlayer((Player) event.getEntity());
            if(match == null) return;
            Player p = ((Player) event.getEntity()).getPlayer();
            if(event.getDamager() instanceof Arrow){
                Arrow arrow = (Arrow)event.getDamager();
                if(arrow.getShooter() instanceof Player){
                    Player p2 = (Player) arrow.getShooter();
                    if(match.Teams.get(p) == match.Teams.get(p2)){
                        event.setCancelled(true);
                    }
                }
            } else if(event.getDamager() instanceof Fireball){
                Fireball fb = (Fireball)event.getDamager();
                ProjectileSource source = TraceEngine.ResolveShooter(fb);
                if(source instanceof Player){
                    Player p2 = (Player) source;
                    if(match.Teams.get(p) == match.Teams.get(p2)){
                        event.setCancelled(true);
                    }
                }
            } else if(event.getDamager() instanceof TNTPrimed){
                TNTPrimed tnt = (TNTPrimed)event.getDamager();
                Optional<UUID> id = match.Tracer.GetSources(tnt).Sources.stream().findAny();
                if(id.isPresent()){
                    Player p2 = Bukkit.getPlayer(id.get());
                    if(p2 != null){
                        if(match.Tracer.IsRedstoneActivated(tnt)){
                            if(event.getFinalDamage() >= p.getHealth()){
                                p.setKiller(p2);
                            }
                        }else if(match.Teams.get(p) == match.Teams.get(p2)){
                            event.setCancelled(true);
                        }
                    }
                }
            } else if(event.getDamager() instanceof Player){
                Player p2 = (Player) event.getDamager();
                if(p2 != null){
                    if(match.Teams.get(p) == match.Teams.get(p2)){
                        event.setCancelled(true);
                    }
                }
            }
        }

    }

    @EventHandler
    public void ExplodeEvent(EntityExplodeEvent e){
        MissileWarsMatch match = LobbyEngine.FromWorld(e.getLocation().getWorld());
        if(match == null) return;
        e.blockList().removeIf(block -> StructureUtils.IsInProtectedRegion(block.getLocation().toVector()));
        if(e.getEntity() instanceof Fireball){
            e.blockList().removeIf(block ->
                    block.getType() != Material.TNT
                            && block.getType() != Material.SLIME_BLOCK
                            && block.getType() != Material.PISTON
                            && block.getType() != Material.PISTON_HEAD
                            && !block.getType().toString().contains("TERRACOTTA")
                            && block.getType() != Material.REDSTONE_BLOCK);
        }
        if(e.getEntity() instanceof TNTPrimed){
            Optional<Block> block = e.blockList().stream().filter(x->x.getType() == CoreGame.GetImpl().GetPortalMaterial()).findAny();
            block.ifPresent(value -> {
                match.EventHandler.PortalChangedEvent(value, (TNTPrimed) e.getEntity());
                PropagatePortalBreak(value);
            });
        }
    }
}
