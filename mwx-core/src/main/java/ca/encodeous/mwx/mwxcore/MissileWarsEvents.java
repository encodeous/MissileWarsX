package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.BalanceStrategy;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class MissileWarsEvents {
    public void PlayerJoinEvent(Player p){
        CoreGame.Instance.mwMatch.AddPlayerToLobby(p);
    }
    public void PlayerLeaveEvent(Player p){
        CoreGame.Instance.mwMatch.RemovePlayer(p);
    }
    public void PlayerMoveEvent(Player p, Location from, Ref<Location> to, Ref<Boolean> cancelled){
        if(to.val.getWorld() == CoreGame.Instance.mwMatch.Map.MswWorld){
            if(CoreGame.Instance.mwConfig.Strategy == BalanceStrategy.PLAYERS_CHOOSE){
                if(CoreGame.Instance.mwMatch.Map.RedJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.Instance.mwMatch.RedPad(p);
                }else if(CoreGame.Instance.mwMatch.Map.GreenJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.Instance.mwMatch.GreenPad(p);
                }
            }else{
                if(CoreGame.Instance.mwMatch.Map.AutoJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.Instance.mwMatch.AutoPad(p);
                }
            }
        }
    }
    public void PlayerInteractEvent(Player p, Action action, BlockFace face, Block target, ItemStack item, Ref<Boolean> cancel, Ref<Boolean> use){
        // check spectate signs
        if(p.getWorld() == CoreGame.Instance.mwMatch.Map.MswWorld) {
            if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR){
                String s = CoreGame.Instance.mwImpl.GetItemId(item);
                if(!s.isEmpty()){
                    CoreGame.Instance.mwMatch.MissileWarsItemInteract(p, action, face, target, s, item, action == Action.RIGHT_CLICK_AIR, cancel, use);
                }
            }
            if(target == null) return;
            if (CoreGame.Instance.mwMatch.Map.ReturnToLobby.contains(target.getLocation().toVector())) {
                CoreGame.Instance.mwMatch.AddPlayerToLobby(p);
            } else if (CoreGame.Instance.mwMatch.Map.Spectate.contains(target.getLocation().toVector())) {
                CoreGame.Instance.mwMatch.AddSpectator(p);
            }
        }
    }
    public void BlockExplodeEvent(Block source, List<Block> blocks, Ref<Float> yield){
        yield.val = (float) 0;
        for(Block block : blocks){
            if(block.getType() == Material.PORTAL){
                if(CoreGame.Instance.mwMatch.Map.RedPortal.IsInBounds(block.getLocation().toVector())){
                    CoreGame.Instance.mwMatch.GreenWin();
                }else{
                    CoreGame.Instance.mwMatch.RedWin();
                }
            }
        }
    }
    public void BlockPhysicsEvent(Block block){
        if(block.getType() == CoreGame.Instance.mwImpl.GetPortalMaterial()){
            if(CoreGame.Instance.mwMatch.hasStarted){
                if(CoreGame.Instance.mwMatch.Map.RedPortal.IsInBounds(block.getLocation().toVector())){
                    CoreGame.Instance.mwMatch.GreenWin();
                }else{
                    CoreGame.Instance.mwMatch.RedWin();
                }
            }
        }
    }
}
