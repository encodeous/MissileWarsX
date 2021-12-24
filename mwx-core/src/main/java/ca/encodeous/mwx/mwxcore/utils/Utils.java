package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.gamestate.*;
import ca.encodeous.mwx.mwxcore.lang.Strings;
import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import util.SpigotReflection;

import java.lang.reflect.Method;
import java.util.logging.Level;

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
    public static int GetPlayerPing(Player p){
        if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_17.getValue()){
            return p.getPing();
        }else{
            return SpigotReflection.get().ping(p);
        }
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
    public static boolean CheckPrivPermission(Player p){
        MissileWarsMatch match = LobbyEngine.FromPlayer(p);
        if(match == null){
            p.sendMessage(Strings.NO_PERMISSION);
            return false;
        }
        if(match instanceof MissileWarsRankedMatch){
            p.sendMessage(Strings.RANKED_PERM_DENIED);
            return false;
        }
        if (!p.hasPermission("mwx.admin")) {
            if (!(match instanceof MissileWarsPracticeMatch)) {
                p.sendMessage(Strings.NO_PERMISSION);
                return false;
            } else {
                if (!match.hasStarted || !(match.IsPlayerInTeam(p, PlayerTeam.Red) || match.IsPlayerInTeam(p, PlayerTeam.Green))) {
                    p.sendMessage(Strings.IN_GAME_COMMAND);
                    return false;
                }
            }
        }
        return true;
    }
    private static Method editSessionMethod = null;
    public static boolean IsLegacy = true;
    private static boolean hasInformed = false;
    public static EditSession GetEditSession(World world){
        // fawe version compatibility
        EditSession ess = null;
        if(IsLegacy){
            try{
                ess = new EditSessionBuilder(BukkitAdapter.adapt(world)).build();
                if(!hasInformed){
                    hasInformed = true;
                    CoreGame.Instance.mwPlugin.getLogger().log(Level.INFO, "Detected & Using Legacy WorldEdit APIs");
                }
            }catch (NoClassDefFoundError e){
                // ignored
                IsLegacy = false;
            }
        }
        if(!IsLegacy){
            try {
                if(editSessionMethod == null){
                    editSessionMethod = WorldEdit.class.getMethod("newEditSession", com.sk89q.worldedit.world.World.class);
                }
                ess = (EditSession) editSessionMethod.invoke(WorldEdit.getInstance(), BukkitAdapter.adapt(world));
            } catch (Exception e) {
                CoreGame.Instance.mwPlugin.getLogger().log(Level.SEVERE, "Unable to access WorldEdit apis, make sure the correct version is installed!");
                e.printStackTrace();
            }
        }
        return ess;
    }
    public static void playBlockSound(Location loc, Material mat) {
        Block curBlock = loc.getBlock();
        Material curMat = curBlock.getType();
        curBlock.setType(mat);
        curBlock.breakNaturally();
        curBlock.setType(curMat);
    }
}
