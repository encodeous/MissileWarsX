package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MissileWarsEventHandler extends ca.encodeous.mwx.mwxcompat1_8.MissileWarsEventHandler {
    private MissileWarsEvents mwEvents;

    public MissileWarsEventHandler(MissileWarsEvents events) {
        super(events);
        mwEvents = events;
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e){
        Ref<Boolean> cancel = new Ref<>(false);
        Ref<Boolean> use = new Ref<>(false);
        mwEvents.PlayerInteractEvent(e.getPlayer(), e.getAction(), e.getBlockFace(), e.getClickedBlock(), e.getItem(), cancel, use);
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
    }
    @EventHandler
    public void ExplodeEvent(EntityExplodeEvent e){
        if(e.getEntity() instanceof Fireball){
            e.blockList().removeIf(block ->
                    block.getType() != Material.TNT
                            && block.getType() != Material.SLIME_BLOCK
                            && block.getType() != Material.PISTON
                            && block.getType() != Material.PISTON_HEAD);
        }
    }
}
