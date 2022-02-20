package ca.encodeous.mwx.core.utils;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.*;
import ca.encodeous.mwx.core.lang.Strings;
import com.fastasyncworldedit.core.extent.clipboard.ReadOnlyClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Stack;
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
    public static boolean CheckPrivPermissionSilent(Player p){
        MissileWarsMatch match = LobbyEngine.FromPlayer(p);
        if(match == null){
            return false;
        }
        if(match instanceof MissileWarsRankedMatch){
            return false;
        }
        if (!p.hasPermission("mwx.admin")) {
            if (!(match instanceof MissileWarsPracticeMatch)) {
                return false;
            } else {
                if (!match.hasStarted || !(match.IsPlayerInTeam(p, PlayerTeam.Red) || match.IsPlayerInTeam(p, PlayerTeam.Green))) {
                    return false;
                }
            }
        }
        return true;
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

    public static Clipboard GetReadonlyClipboard(EditSession session, Region srcRegion){
        return ReadOnlyClipboard.of(
                session, srcRegion, false, false
        );
    }

    public static void SetTo(PasteBuilder pb, CuboidRegion region){
        pb.to(region.getMinimumPoint());
    }

    public static void SetOrigin(Clipboard pb, CuboidRegion region){
        pb.setOrigin(region.getMinimumPoint());
    }

    public static PasteBuilder createPaste(ClipboardHolder clipboard, Extent targetExtent, World world){
        return clipboard.createPaste(targetExtent);
    }

    public static ClipboardHolder GetHolder(Clipboard clipboard, World world){
        return new ClipboardHolder(clipboard);
    }

    public static EditSession GetEditSession(World world){
        return WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world));
    }

    public static CuboidRegion GetRegion(Bounds bounds, World world){
        return new CuboidRegion(
                BukkitAdapter.asVector(Utils.LocationFromVec(bounds.Min, world)).toBlockPoint(),
                BukkitAdapter.asVector(Utils.LocationFromVec(bounds.Max, world)).toBlockPoint()
        );
    }

    public static void WeSetBlock(EditSession es, Material mat, Region region){
        es.setBlocks(region, BukkitAdapter.asBlockType(mat));
    }

    public static void playBlockSound(Location loc, Material mat) {
        Block curBlock = loc.getBlock();
        Material curMat = curBlock.getType();
        curBlock.setType(mat);
        curBlock.breakNaturally();
        curBlock.setType(curMat);
    }
    public static void CheckPath(File checked){
        if(Files.isSymbolicLink(checked.toPath())){
            throw new RuntimeException("Symbolic links are not allowed to be deleted due to security issues. Please contact your server administrator. Path: " + checked.getAbsolutePath());
        }
    }
    public static void DeleteFolder(File folder){
        if(!folder.exists()) return;
        CheckPath(folder);
        var stk = new Stack<File>();
        stk.push(folder);
        while(!stk.empty()){
            var f = stk.pop();
            if(!f.isDirectory() || !f.exists()) continue;
            for(var sub : f.listFiles()){
                CheckPath(sub);
                if(sub.isDirectory()){
                    stk.push(sub);
                }else{
                    if(!sub.delete()){
                        sub.deleteOnExit();
                    }
                }
            }
            if(!f.delete()){
                f.deleteOnExit();
            }
        }
    }
}
