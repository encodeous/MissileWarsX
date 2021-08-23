package ca.encodeous.mwx.mwxcompat1_8;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceType;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import ca.encodeous.mwx.soundengine.SoundType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;

import static ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine.PropagatePortalBreak;
import static org.bukkit.Bukkit.getServer;

public class MissileWarsEventHandler implements Listener {
    private final MissileWarsEvents mwEvents;
    public MissileWarsEventHandler(MissileWarsEvents events){
        mwEvents = events;
    }
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        event.setJoinMessage("");
        mwEvents.PlayerJoinEvent(event.getPlayer());
    }
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.BEDROCK || event.getBlock().getType() == Material.OBSIDIAN){
            event.setCancelled(true);
        }
        if(event.getBlock().getType() == CoreGame.GetImpl().GetPortalMaterial()){
            PropagatePortalBreak(event.getBlock());
        }
        CoreGame.GetMatch().Tracer.RemoveBlock(event.getBlock().getLocation().toVector());
    }
    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event){
        if(event.getBlock().getType() == Material.TNT){
            CoreGame.GetMatch().Tracer.AddBlock(event.getPlayer().getUniqueId(), TraceType.TNT, event.getBlock().getLocation().toVector());
        }else if(event.getBlock().getType() == Material.REDSTONE_BLOCK){
            CoreGame.GetMatch().Tracer.AddBlock(event.getPlayer().getUniqueId(), TraceType.REDSTONE, event.getBlock().getLocation().toVector());
        }
    }
    @EventHandler
    public void FoodChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent event){
        event.setQuitMessage(Formatter.FCL(event.getPlayer().getDisplayName() + " has left the game."));
        mwEvents.PlayerLeaveEvent(event.getPlayer());
        if(Bukkit.getOnlinePlayers().size() == 1 && CoreGame.GetMatch().hasStarted || CoreGame.GetMatch().isStarting){
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, new Runnable() {
                @Override
                public void run() {
                    CoreGame.Instance.EndMatch();
                }
            }, 10);
        }
    }
    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event){
        Ref<Boolean> cancelled = new Ref<>(false);
        Ref<Location> to = new Ref<>(event.getTo());
        mwEvents.PlayerMoveEvent(event.getPlayer(), to, cancelled);
        event.setTo(to.val);
        if(cancelled.val){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void BlockExplodeEvent(BlockExplodeEvent event){
        event.setYield(0);
    }
    @EventHandler
    public void ExplodeEvent(EntityExplodeEvent e){
        e.blockList().removeIf(block -> CoreGame.GetMatch().IsInProtectedRegion(block.getLocation().toVector()));
        if(e.getEntity() instanceof Fireball){
            e.blockList().removeIf(block ->
                    block.getType() != Material.TNT
                            && block.getType() != Material.SLIME_BLOCK
                            && block.getType() != Material.PISTON_BASE
                            && block.getType() != Material.PISTON_EXTENSION
                            && block.getType() != Material.PISTON_STICKY_BASE
                            && block.getType() != Material.PISTON_MOVING_PIECE
                            && block.getType() != Material.STAINED_CLAY
                            && block.getType() != Material.REDSTONE_BLOCK
            );
        }
        if(e.getEntity() instanceof TNTPrimed){
            Optional<Block> block = e.blockList().stream().filter(x->x.getType() == CoreGame.GetImpl().GetPortalMaterial()).findAny();
            block.ifPresent(value -> {
                mwEvents.PortalChangedEvent(value, (TNTPrimed) e.getEntity());
                PropagatePortalBreak(value);
            });
        }
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Fireball && e.getEntity().getVehicle() != null && e.getEntity().getVehicle().getType() == EntityType.ARMOR_STAND && !(e.getDamager() instanceof TNTPrimed)){
            Entity vehicle = e.getEntity().getVehicle();
            if(e.getDamager() instanceof Projectile) {
                ((Fireball) e.getEntity()).setDirection(e.getDamager().getVelocity().normalize());
                ((Fireball) e.getEntity()).setShooter(TraceEngine.ResolveShooter((Projectile) e.getDamager()));
            }else{
                ((Fireball) e.getEntity()).setShooter((ProjectileSource) e.getDamager());
            }
            e.getEntity().leaveVehicle();
            vehicle.remove();
        }
    }

    @EventHandler
    public void PlayerDamageEvent(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(!CoreGame.GetMatch().IsPlayerInTeam(p, PlayerTeam.Red) && !CoreGame.GetMatch().IsPlayerInTeam(p, PlayerTeam.Green)){
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {
        if(CoreGame.GetMatch().Teams.containsKey(e.getPlayer()))
            e.setRespawnLocation(CoreGame.GetMatch().GetTeamSpawn(CoreGame.GetMatch().Teams.get(e.getPlayer())));
    }
    @EventHandler
    public void PistonPushEvent(BlockPistonExtendEvent e){
        CoreGame.GetMatch().Tracer.TransformBlocks(e.getBlocks(), e.getDirection());
    }
    @EventHandler
    public void PistonPullEvent(BlockPistonRetractEvent e){
        CoreGame.GetMatch().Tracer.TransformBlocks(e.getBlocks(), e.getDirection().getOppositeFace());
    }
    @EventHandler
    public void PlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if(p.getKiller() != null){
            if(CoreGame.GetMatch().Teams.get(p) != CoreGame.GetMatch().Teams.get(p.getKiller())){
                CoreGame.GetImpl().PlaySound(p.getKiller(), SoundType.KILL_OTHER);
            }else{
                CoreGame.GetImpl().PlaySound(p.getKiller(), SoundType.KILL_TEAM);
            }
        }
        getServer().getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, () -> {
            try {
                p.spigot().respawn();
                CoreGame.GetImpl().PlaySound(p, SoundType.RESPAWN);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }, 2);
    }
    @EventHandler
    public void BlockPhysicsEvent(BlockPhysicsEvent e){
        if(e.getChangedType() == CoreGame.GetImpl().GetPortalMaterial()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void ArmourStandEvent(PlayerArmorStandManipulateEvent e){
        e.setCancelled(true);
    }
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e){
        Ref<Boolean> cancel = new Ref<>(false);
        Ref<Boolean> use = new Ref<>(false);
        mwEvents.PlayerInteractEvent(e.getPlayer(), e.getAction(), e.getBlockFace(), e.getClickedBlock(), e.getItem(), cancel, use);
        if(use.val){
            ItemStack item = e.getPlayer().getInventory().getItemInHand();
            if(item.getAmount() == 1){
                item.setType(Material.AIR);
            }else{
                item.setAmount(item.getAmount() - 1);
            }
            e.getPlayer().getInventory().setItemInHand(item);
        }
        if(cancel.val){
            e.setCancelled(true);
        }
        Player p = (Player) e.getPlayer().getInventory().getHolder();
        ReequipGunblade(p);
    }
    public void ReequipGunblade(Player p){
        if(p.getGameMode() == GameMode.CREATIVE) return;
        if(CoreGame.GetMatch().IsPlayerInTeam(p, PlayerTeam.Green) || CoreGame.GetMatch().IsPlayerInTeam(p, PlayerTeam.Red)){
            if(CoreGame.GetMatch().CountItem(p, CoreGame.Instance.GetItemById(MissileWarsCoreItem.GUNBLADE.getValue())) == 0){
                CoreGame.GetImpl().EquipPlayer(p, CoreGame.GetMatch().IsPlayerInTeam(p, PlayerTeam.Red));
            }
        }
    }
    @EventHandler
    public void PlayerInventoryInteractEvent(InventoryClickEvent event){
        if(event.getInventory().getHolder() instanceof Player){
            Player p = (Player) event.getInventory().getHolder();
            if(p.getGameMode() == GameMode.CREATIVE) return;
            ReequipGunblade(p);
        }
        if(event.getSlotType() == InventoryType.SlotType.ARMOR){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void ItemDropEvent(PlayerDropItemEvent event){
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(event.getItemDrop().getItemStack().getType() != Material.ARROW){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void ProjectileHitEvent(ProjectileHitEvent event){
        if(event.getEntity() instanceof Snowball){
            CoreGame.GetMatch().AliveSnowballs.remove(event.getEntity().getUniqueId());
        }
    }
}
