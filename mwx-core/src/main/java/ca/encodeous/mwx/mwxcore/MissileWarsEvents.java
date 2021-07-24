package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.BalanceStrategy;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class MissileWarsEvents {
    public void PlayerJoinEvent(Player p){
        CoreGame.GetMatch().AddPlayerToTeam(p, PlayerTeam.None);
    }
    public void PlayerLeaveEvent(Player p){
        CoreGame.GetMatch().RemovePlayer(p);
    }
    public void PlayerMoveEvent(Player p, Ref<Location> to, Ref<Boolean> cancelled){
        if(to.val.getWorld() == CoreGame.GetMatch().Map.MswWorld){
            if(CoreGame.Instance.mwConfig.Strategy == BalanceStrategy.PLAYERS_CHOOSE){
                if(CoreGame.GetMatch().Map.RedJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.GetMatch().RedPad(p);
                }else if(CoreGame.GetMatch().Map.GreenJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.GetMatch().GreenPad(p);
                }
            }else{
                if(CoreGame.GetMatch().Map.AutoJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    CoreGame.GetMatch().AutoPad(p);
                }
            }
        }
    }
    public void PlayerInteractEvent(Player p, Action action, BlockFace face, Block target, ItemStack item, Ref<Boolean> cancel, Ref<Boolean> use){
        // check spectate signs
        if(p.getWorld() == CoreGame.GetMatch().Map.MswWorld) {
            if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR){
                String s = CoreGame.GetImpl().GetItemId(item);
                if(!s.isEmpty()){
                    CoreGame.GetMatch().MissileWarsItemInteract(p, action, face, target, s, item, action == Action.RIGHT_CLICK_AIR, cancel, use);
                }
            }
            if(target == null) return;
            if (CoreGame.GetMatch().Map.ReturnToLobby.contains(target.getLocation().toVector())) {
                CoreGame.GetMatch().AddPlayerToTeam(p, PlayerTeam.None);
            } else if (CoreGame.GetMatch().Map.Spectate.contains(target.getLocation().toVector())) {
                CoreGame.GetMatch().AddPlayerToTeam(p, PlayerTeam.Spectator);
            }
        }
    }
    public void PortalChangedEvent(Block block, TNTPrimed entity){
        if(block.getType() == CoreGame.GetImpl().GetPortalMaterial()){
            if(CoreGame.GetMatch().hasStarted){
                HashSet<UUID> credits = CoreGame.GetMatch().Tracer.FindCause(entity);
                ArrayList<Player> players = new ArrayList<>();
                for(UUID id : credits){
                    if(Bukkit.getPlayer(id) != null){
                        players.add(Bukkit.getPlayer(id));
                    }
                }
                if(CoreGame.GetMatch().Map.RedPortal.IsInBounds(block.getLocation().toVector())){
                    CoreGame.GetMatch().GreenWin(players);
                }else{
                    CoreGame.GetMatch().RedWin(players);
                }
            }
        }
    }
}
