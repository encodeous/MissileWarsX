package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.data.SoundType;
import ca.encodeous.mwx.data.TraceType;
import ca.encodeous.mwx.engines.trace.TraceEngine;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.structure.StructureUtils;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.item.SpecialItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;
import java.util.UUID;

import static ca.encodeous.mwx.engines.trace.TraceEngine.PropagatePortalBreak;
import static org.bukkit.Bukkit.getServer;

public class MissileWarsEventHandler implements Listener {

    @EventHandler
    public void ItemDropEvent(PlayerDropItemEvent event){
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(event.getItemDrop().getItemStack().getType() != Material.ARROW){
            event.setCancelled(true);
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

    public void ReequipGunblade(Player p){
        if(p.getGameMode() == GameMode.CREATIVE) return;
        MissileWarsMatch match = LobbyEngine.FromPlayer(p);
        if(match != null){
            if(match.IsPlayerInTeam(p, PlayerTeam.Green) || match.IsPlayerInTeam(p, PlayerTeam.Red)){
                if(Utils.CountItem(p, CoreGame.Instance.GetItemById(MissileWarsCoreItem.GUNBLADE.getValue())) == 0){
                    CoreGame.GetImpl().EquipPlayer(p, match.IsPlayerInTeam(p, PlayerTeam.Red));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void BlockPhysicsEvent(BlockPhysicsEvent e){
        if(e.getChangedType() == CoreGame.GetImpl().GetPortalMaterial()
                || e.getBlock().getType() == CoreGame.GetImpl().GetPortalMaterial()){
            e.setCancelled(true);
        }
        MissileWarsMatch match = LobbyEngine.FromWorld(e.getBlock().getWorld());
        if(match != null){
            if(match.Map.isBusy){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        MissileWarsMatch match = LobbyEngine.FromPlayer(p);
        if(match != null){
            if(p.getKiller() != null){
                if(match.Teams.get(p) != match.Teams.get(p.getKiller())){
                    CoreGame.GetImpl().PlaySound(p.getKiller(), SoundType.KILL_OTHER);
                }else{
                    CoreGame.GetImpl().PlaySound(p.getKiller(), SoundType.KILL_TEAM);
                }
                match.Kills.put(p.getKiller().getUniqueId(), match.Deaths.getOrDefault(p.getKiller().getUniqueId(), 0) + 1);
            }
            match.Deaths.put(p.getUniqueId(), match.Deaths.getOrDefault(p.getUniqueId(), 0) + 1);
            //match.lobby.SendMessage(e.getDeathMessage());
            //e.setDeathMessage(null);
        }
        if(MCVersion.QueryVersion().getValue() < MCVersion.v1_15.getValue()){
            getServer().getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, () -> {
                try {
                    p.spigot().respawn();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }, 2);
        }
    }

    @EventHandler
    public void PistonPullEvent(BlockPistonRetractEvent e){
        MissileWarsMatch match = LobbyEngine.FromWorld(e.getBlock().getWorld());
        if(match != null){
            match.Tracer.TransformBlocks(e.getBlocks(), e.getDirection().getOppositeFace());
        }
    }

    @EventHandler
    public void PistonPushEvent(BlockPistonExtendEvent e){
        MissileWarsMatch match = LobbyEngine.FromWorld(e.getBlock().getWorld());
        if(match != null){
            match.Tracer.TransformBlocks(e.getBlocks(), e.getDirection());
        }
    }

    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {
        MissileWarsMatch match = LobbyEngine.FromPlayer(e.getPlayer());
        if(match != null){
            if(match.Teams.containsKey(e.getPlayer()))
                e.setRespawnLocation(Utils.GetTeamSpawn(match.Teams.get(e.getPlayer()), match));
            CoreGame.GetImpl().PlaySound(e.getRespawnLocation(), SoundType.RESPAWN);
        }
    }

    @EventHandler
    public void PlayerDamageEvent(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            MissileWarsMatch match = LobbyEngine.FromPlayer(p);
            if(match != null){
                if(match.Map.isBusy){
                    e.setCancelled(true);
                    return;
                }
                if(!match.IsPlayerInTeam(p, PlayerTeam.Red) && !match.IsPlayerInTeam(p, PlayerTeam.Green)){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Fireball && e.getEntity().getVehicle() != null && e.getEntity().getVehicle().getType() == EntityType.ARMOR_STAND){
            Entity vehicle = e.getEntity().getVehicle();
            if(e.getDamager() instanceof Projectile) {
                ((Fireball) e.getEntity()).setDirection(e.getDamager().getVelocity().normalize());
                ((Fireball) e.getEntity()).setShooter(TraceEngine.ResolveShooter((Projectile) e.getDamager()));
            }else if(e.getDamager() instanceof TNTPrimed){
                TNTPrimed tnt = (TNTPrimed)e.getDamager();
                ((Fireball) e.getEntity()).setDirection(e.getDamager().getLocation().toVector().subtract(e.getEntity().getLocation().toVector()));
                e.getEntity().setVelocity(e.getDamager().getLocation().toVector().subtract(e.getEntity().getLocation().toVector()));
                if(tnt.getSource() != null && tnt.getSource() instanceof ProjectileSource){
                    ((Fireball) e.getEntity()).setShooter((ProjectileSource) tnt.getSource());
                }
            }else{
                ((Fireball) e.getEntity()).setShooter((ProjectileSource) e.getDamager());
            }
            e.getEntity().leaveVehicle();
            vehicle.setCustomName("removed");
            vehicle.remove();
        }
    }

    @EventHandler
    public void BlockExplodeEvent(BlockExplodeEvent event){
        event.setYield(0);
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event){
        MissileWarsMatch match = LobbyEngine.FromPlayer(event.getPlayer());
        if(match == null) return;
        Ref<Location> to = new Ref<>(event.getTo());
        match.EventHandler.PlayerMoveEvent(event.getPlayer(), to);
        event.setTo(to.val);
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent event){
        CoreGame.Instance.PlayerLeft(event.getPlayer());
        event.setQuitMessage("");
    }

    @EventHandler
    public void FoodChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event){
        MissileWarsMatch match = LobbyEngine.FromWorld(event.getBlock().getWorld());
        if(match != null){
            if(match.Map.isBusy){
                event.setCancelled(true);
                return;
            }
            if(StructureUtils.IsInProtectedRegion(event.getBlock().getLocation().toVector())){
                if(!match.AllowPlayerInteractProtectedRegion(event.getPlayer())) event.setCancelled(true);
            }
            if(event.getBlock().getType() == Material.TNT){
                match.Tracer.AddBlock(event.getPlayer().getUniqueId(), TraceType.TNT, event.getBlock().getLocation().toVector());
            }else if(event.getBlock().getType() == Material.REDSTONE_BLOCK){
                match.Tracer.AddBlock(event.getPlayer().getUniqueId(), TraceType.REDSTONE, event.getBlock().getLocation().toVector());
            }
        }
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        MissileWarsMatch match = LobbyEngine.FromWorld(event.getPlayer().getWorld());
        if(match == null) return;
        if(!match.EventHandler.BlockBreakEvent(event.getPlayer(), event.getBlock())) event.setCancelled(true);
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        CoreGame.Instance.PlayerJoined(event.getPlayer());
        event.setJoinMessage("");
    }

    @EventHandler
    public void ArmorStandEvent(PlayerArmorStandManipulateEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e){
        Ref<Boolean> cancel = new Ref<>(false);
        Ref<Boolean> use = new Ref<>(false);
        MissileWarsMatch match = LobbyEngine.FromPlayer(e.getPlayer());
        if(match == null) return;
        if(!(match instanceof MissileWarsRankedMatch)) {
            if(e.getItem() != null) {
                ItemMeta meta = e.getItem().getItemMeta();
                if(meta != null && meta.hasCustomModelData()) {
                    for (SpecialItem item : SpecialItem.ITEMS) {
                        if(e.getItem().getType() == item.getItem() && meta.getCustomModelData() == item.getData()) {
                            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                item.onUse(e);
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
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

    @EventHandler
    public void MoveEvent(PlayerMoveEvent e){
        Player p = e.getPlayer();
        var match = LobbyEngine.FromPlayer(p);
        if(match != null){
            if(!match.Map.WorldMaxBoundingBox.IsInBounds(e.getTo().toVector())){
                if(p.isInsideVehicle()){
                    p.getVehicle().eject();
                }
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler
    public void TeleportEvent(PlayerTeleportEvent e){
        var match = LobbyEngine.FromWorld(e.getPlayer().getWorld());
        if(match != null){
            if(!match.Map.WorldMaxBoundingBox.IsInBounds(e.getTo().toVector())){
                e.setTo(e.getFrom());
            }
        }
    }
}
