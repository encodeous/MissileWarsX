package ca.encodeous.mwx.mwxcompat1_8;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class MissileWarsEventHandler implements Listener {
    private MissileWarsEvents mwEvents;
    public MissileWarsEventHandler(MissileWarsEvents events){
        mwEvents = events;
    }
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        event.setJoinMessage("");
        mwEvents.PlayerJoinEvent(event.getPlayer());
    }
    @EventHandler
    public void FoodChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent event){
        event.setQuitMessage("");
        mwEvents.PlayerLeaveEvent(event.getPlayer());
    }
    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event){
        Ref<Boolean> cancelled = new Ref<>(false);
        Ref<Location> to = new Ref<>(event.getTo());
        mwEvents.PlayerMoveEvent(event.getPlayer(), event.getFrom(), to, cancelled);
        event.setTo(to.val);
        if(cancelled.val){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void BlockExplodeEvent(BlockExplodeEvent event){
        Ref<Float> yield = new Ref<>(event.getYield());
        mwEvents.BlockExplodeEvent(event.getBlock(), event.blockList(), yield);
        event.setYield(yield.val);
    }
    @EventHandler
    public void ExplodeEvent(EntityExplodeEvent e){
        if(e.getEntity() instanceof Fireball){
            e.blockList().removeIf(block ->
                    block.getType() != Material.TNT
                            && block.getType() != Material.SLIME_BLOCK
                            && block.getType() != Material.PISTON_BASE
                            && block.getType() != Material.PISTON_EXTENSION
                            && block.getType() != Material.PISTON_STICKY_BASE
                            && block.getType() != Material.PISTON_MOVING_PIECE
            );
        }
    }
    @EventHandler
    public void EntityDamageEvent(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Fireball && e.getEntity().getVehicle() != null && e.getEntity().getVehicle().getType() == EntityType.ARMOR_STAND && e.getDamager() instanceof Player){
            Entity vehicle = e.getEntity().getVehicle();
            e.getEntity().leaveVehicle();
            vehicle.remove();
        }
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
            boolean rt = CoreGame.Instance.mwMatch.IsPlayerInTeam((Player) e.getEntity(), PlayerTeam.Red);
            boolean rt1 = CoreGame.Instance.mwMatch.IsPlayerInTeam((Player) e.getDamager(), PlayerTeam.Red);
            if(rt == rt1){
                // prevent friendly fire
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void ArmourStandEvent(PlayerArmorStandManipulateEvent e){
        e.setCancelled(true);
    }
    @EventHandler
    public void BlockPhysicsEvent(BlockPhysicsEvent event){
        mwEvents.BlockPhysicsEvent(event.getBlock());
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
    }
    @EventHandler
    public void PlayerInventoryInteractEvent(InventoryClickEvent event){
        if(event.getSlotType() == InventoryType.SlotType.ARMOR){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void ItemDropEvent(PlayerDropItemEvent event){
        event.setCancelled(true);
    }
    @EventHandler
    public void ProjectileHitEvent(ProjectileHitEvent event){
        if(event.getEntity() instanceof Snowball){
            CoreGame.Instance.mwMatch.AliveSnowballs.remove(event.getEntity().getUniqueId());
        }
    }
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event){
        CoreGame.Instance.mwMatch.TeamColorBroadcast(event.getEntity(), event.getDeathMessage());
        event.setDeathMessage("");
        CoreGame.Instance.mwMatch.RespawnPlayer(event.getEntity());
    }
}
