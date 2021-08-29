package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Utils {
    public static Location LocationFromVec(Vector vec, World w){
        return new Location(w, vec.getX(), vec.getY(), vec.getZ());
    }
    public static Vector GetRandomNormalizedVector(){
        Vector v = Vector.getRandom();
        v.setX(v.getX() - 0.5f);
        v.setZ(v.getZ() - 0.5f);
        v.setY(v.getY() - 0.5f);
        return v.normalize();
    }
    public static int CountItem(Player p, MissileWarsItem item){
        int curCnt = 0;
        if(CoreGame.GetImpl().GetItemId(p.getItemOnCursor()).equals(item.MissileWarsItemId)){
            curCnt += p.getItemOnCursor().getAmount();
        }
        for(ItemStack i : p.getOpenInventory().getBottomInventory()){
            String id = CoreGame.GetImpl().GetItemId(i);
            if(id.equals(item.MissileWarsItemId)){
                curCnt += i.getAmount();
            }
        }
        for(ItemStack i : p.getOpenInventory().getTopInventory()){
            String id = CoreGame.GetImpl().GetItemId(i);
            if(id.equals(item.MissileWarsItemId)){
                curCnt += i.getAmount();
            }
        }
        return curCnt;
    }
    public static Location GetTeamSpawn(PlayerTeam team, MissileWarsMatch match){
        boolean hasStarted = match.hasStarted;
        MissileWarsMap map = match.Map;
        if(hasStarted){
            if(team == PlayerTeam.Green){
                Location loc = Utils.LocationFromVec(map.GreenSpawn, map.MswWorld);
                loc.setYaw(map.GreenYaw);
                loc.setPitch(0);
                return loc;
            }else if(team == PlayerTeam.Red){
                Location loc = Utils.LocationFromVec(map.RedSpawn, map.MswWorld);
                loc.setYaw(map.RedYaw);
                loc.setPitch(0);
                return loc;
            }else{
                Location loc = Utils.LocationFromVec(map.Spawn, map.MswWorld);
                loc.setYaw(map.SpawnYaw);
                loc.setPitch(0);
                return loc;
            }
        }else{
            Location loc = null;
            if(team == PlayerTeam.Green){
                loc = Utils.LocationFromVec(map.GreenLobby, map.MswWorld);
            }else if(team == PlayerTeam.Red){
                loc = Utils.LocationFromVec(map.RedLobby, map.MswWorld);
            }else{
                loc = Utils.LocationFromVec(map.Spawn, map.MswWorld);
            }
            loc.setYaw(map.SpawnYaw);
            loc.setPitch(0);
            return loc;
        }
    }
}
