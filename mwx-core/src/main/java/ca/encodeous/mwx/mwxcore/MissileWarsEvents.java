package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
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
    private MissileWarsMatch match;
    public MissileWarsEvents(MissileWarsMatch match){
        this.match = match;
    }
    public void PlayerJoinEvent(Player p){
        match.AddPlayerToTeam(p, PlayerTeam.None);
    }
    public void PlayerLeaveEvent(Player p){
        match.RemovePlayer(p);
    }
    public void PlayerMoveEvent(Player p, Ref<Location> to){
        if(to.val.getWorld() == match.Map.MswWorld){
            if(match.Map.SeparateJoin){
                if(match.Map.RedJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    match.RedPad(p);
                }else if(match.Map.GreenJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    match.GreenPad(p);
                }
            }else{
                if(match.Map.AutoJoin.contains(to.val.getBlock().getRelative(BlockFace.DOWN).getLocation().toVector())){
                    match.AutoPad(p);
                }
            }
        }
    }
    public void PlayerInteractEvent(Player p, Action action, Block target, ItemStack item, Ref<Boolean> cancel, Ref<Boolean> use){
        // check spectate signs
        if(p.getWorld() == match.Map.MswWorld) {
            if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR){
                String s = CoreGame.GetImpl().GetItemId(item);
                if(!s.isEmpty()){
                    match.MissileWarsItemInteract(p, target, s, item, action == Action.RIGHT_CLICK_AIR, cancel, use);
                }
            }
            if(target == null) return;
            if (match.Map.ReturnToLobby.contains(target.getLocation().toVector())) {
                match.AddPlayerToTeam(p, PlayerTeam.None);
            } else if (match.Map.Spectate.contains(target.getLocation().toVector())) {
                match.AddPlayerToTeam(p, PlayerTeam.Spectator);
            }
        }
    }
    public boolean PortalChangedEvent(Block block, TNTPrimed entity){
        if(!match.Map.RedPortal.IsInBounds(block.getLocation().toVector()) &&
                !match.Map.GreenPortal.IsInBounds(block.getLocation().toVector())) return false;
        if(block.getType() == CoreGame.GetImpl().GetPortalMaterial()){
            if(match.hasStarted){
                HashSet<UUID> credits = match.Tracer.FindCause(entity);
                ArrayList<Player> players = new ArrayList<>();
                for(UUID id : credits){
                    if(Bukkit.getPlayer(id) != null){
                        players.add(Bukkit.getPlayer(id));
                    }
                }
                match.PortalBroken(match.Map.RedPortal.IsInBounds(block.getLocation().toVector()), players);
            }
        }
        return true;
    }
}
