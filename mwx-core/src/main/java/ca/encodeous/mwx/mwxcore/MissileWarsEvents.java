package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.BalanceStrategy;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class MissileWarsEvents {
    public void PlayerJoinEvent(Player p){
        CoreGame.Instance.mwMatch.AddPlayerToTeam(p, PlayerTeam.None);
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
                CoreGame.Instance.mwMatch.AddPlayerToTeam(p, PlayerTeam.None);
            } else if (CoreGame.Instance.mwMatch.Map.Spectate.contains(target.getLocation().toVector())) {
                CoreGame.Instance.mwMatch.AddPlayerToTeam(p, PlayerTeam.Spectator);
            }
        }
    }
    public void PortalChangedEvent(Block block, TNTPrimed entity){
        if(block.getType() == CoreGame.Instance.mwImpl.GetPortalMaterial()){
            if(CoreGame.Instance.mwMatch.hasStarted){
                HashSet<UUID> credits = CoreGame.Instance.mwMatch.Tracer.FindCause(entity);
                ArrayList<Player> players = new ArrayList<>();
                for(UUID id : credits){
                    if(Bukkit.getPlayer(id) != null){
                        players.add(Bukkit.getPlayer(id));
                    }
                }
                if(CoreGame.Instance.mwMatch.Map.RedPortal.IsInBounds(block.getLocation().toVector())){
                    CoreGame.Instance.mwMatch.GreenWin(players);
                }else{
                    CoreGame.Instance.mwMatch.RedWin(players);
                }
            }
        }
    }
}
