package ca.encodeous.mwx.core.utils;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.*;
import ca.encodeous.mwx.core.lang.Strings;
import com.fastasyncworldedit.core.extent.clipboard.ReadOnlyClipboard;
import com.fastasyncworldedit.core.util.EditSessionBuilder;
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
    private static Method editSessionMethod = null;
    public static boolean IsLegacy = true;
    public static boolean IsWe6 = false;
    private static boolean hasInformed = false;

    private static Class<?> esb() throws ClassNotFoundException {
        return Class.forName("com.boydti.fawe.util.EditSessionBuilder");
    }
    private static Class<?> weworld() throws ClassNotFoundException {
        return Class.forName("com.sk89q.worldedit.world.World");
    }

    private static boolean hasDetected = false;

    public static void DetectWe(){
        World world = Bukkit.getWorlds().get(0);
        if(hasDetected) return;
        hasDetected = true;
        EditSession ess = null;
        if(MCVersion.QueryVersion().getValue() < MCVersion.v1_13.getValue()){
            IsWe6 = true;
            if(!hasInformed){
                hasInformed = true;
                CoreGame.Instance.mwPlugin.getLogger().log(Level.WARNING, "Detected & Using Extremely Legacy WorldEdit 6 APIs");
            }
            return;
        }
        if(IsLegacy){
            try{
                try{
                    ess = new EditSessionBuilder(BukkitAdapter.adapt(world)).build();
                }catch (Exception e){
                    IsWe6 = true;
                }
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
            try{
                if(editSessionMethod == null){
                    editSessionMethod = WorldEdit.class.getMethod("newEditSession", com.sk89q.worldedit.world.World.class);
                }
                ess = (EditSession) editSessionMethod.invoke(WorldEdit.getInstance(), BukkitAdapter.adapt(world));
            } catch (Exception e) {
                CoreGame.Instance.mwPlugin.getLogger().log(Level.SEVERE, "Unable to access WorldEdit apis, make sure the correct version is installed!");
                e.printStackTrace();
            }
        }
    }

    public static Clipboard GetReadonlyClipboard(EditSession session, Region srcRegion){
        DetectWe();
        if(IsWe6){
            try {
                Class<?> clazz = Class.forName("com.boydti.fawe.object.clipboard.ReadOnlyClipboard");
                Class<?> clazz2 = Class.forName("com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard");
                Class<?> clazz3 = Class.forName("com.boydti.fawe.object.clipboard.FaweClipboard");
                Object roc = Reflection.getMethod(clazz, "of", EditSession.class, Region.class, boolean.class, boolean.class)
                        .invoke(null, session, srcRegion, false, false);
                Object bac = Reflection.newInstance(Reflection.getConstructor(clazz2, Region.class, clazz3), srcRegion, roc);
                return (Clipboard) bac;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }else{
            return ReadOnlyClipboard.of(
                    session, srcRegion, false, false
            );
        }
    }

    public static void SetTo(PasteBuilder pb, CuboidRegion region){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.sk89q.worldedit.Vector");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object vec = null;
            try {
                vec = Reflection.getMethod(CuboidRegion.class, "getMinimumPoint").invoke(region);
                Reflection.getMethod(PasteBuilder.class, "to", clazz).invoke(pb, vec);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            pb.to(region.getMinimumPoint());
        }
    }

    public static void SetOrigin(Clipboard pb, CuboidRegion region){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.sk89q.worldedit.Vector");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object vec = null;
            try {
                vec = Reflection.getMethod(CuboidRegion.class, "getMinimumPoint").invoke(region);
                Reflection.getMethod(Clipboard.class, "setOrigin", clazz).invoke(pb, vec);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            pb.setOrigin(region.getMinimumPoint());
        }
    }

    public static PasteBuilder createPaste(ClipboardHolder clipboard, Extent targetExtent, World world){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> clazz3 = null;
            try {
                clazz3 = Class.forName("com.sk89q.worldedit.world.registry.WorldData");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object weWorld = Reflection.newInstance(Reflection.getConstructor(clazz, World.class), world);
            Object weWorldData = null;
            try {
                weWorldData = Reflection.getMethod(clazz, "getWorldData").invoke(weWorld);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            try {
                return (PasteBuilder) Reflection.getMethod(ClipboardHolder.class, "createPaste", Extent.class, clazz3).invoke(clipboard, targetExtent, weWorldData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }else{
            return clipboard.createPaste(targetExtent);
        }
    }

    public static ClipboardHolder GetHolder(Clipboard clipboard, World world){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> clazz2 = null;
            try {
                clazz2 = Class.forName("com.sk89q.worldedit.session.ClipboardHolder");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> clazz3 = null;
            try {
                clazz3 = Class.forName("com.sk89q.worldedit.world.registry.WorldData");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object weWorld = Reflection.newInstance(Reflection.getConstructor(clazz, World.class), world);
            Object weWorldData = null;
            try {
                weWorldData = Reflection.getMethod(clazz, "getWorldData").invoke(weWorld);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ClipboardHolder) Reflection.newInstance(Reflection.getConstructor(clazz2, Clipboard.class, clazz3), clipboard, weWorldData);
        }else{
            return new ClipboardHolder(clipboard);
        }
    }

    public static EditSession GetEditSession(World world){
        // fawe version compatibility
        EditSession ess = null;
        DetectWe();
        if(IsWe6){
            Object esb = null;
            try {
                esb = Reflection.newInstance(Reflection.getConstructor(esb(), String.class), world.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                ess = (EditSession)Reflection.getMethod(esb(), "build").invoke(esb);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else if(IsLegacy) {
            try {
                try {
                    ess = new EditSessionBuilder(BukkitAdapter.adapt(world)).build();
                } catch (Exception e) {
                    IsWe6 = true;
                }
            } catch (NoClassDefFoundError e) {
                // ignored
                IsLegacy = false;
            }
        }
        else{
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

    public static Object We6Vec(int x, int y, int z){
        Class<?> clazz = null;
        try {
            clazz = Class.forName("com.sk89q.worldedit.Vector");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Reflection.newInstance(Reflection.getConstructor(clazz, int.class, int.class, int.class), x, y, z);
    }

    public static CuboidRegion GetRegion(Bounds bounds, World world){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> vClazz = null;
            try {
                vClazz = Class.forName("com.sk89q.worldedit.Vector");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object weWorld = Reflection.newInstance(Reflection.getConstructor(clazz, World.class), world);
            Class<?> cbR = null;
            try {
                cbR = Class.forName("com.sk89q.worldedit.regions.CuboidRegion");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                return (CuboidRegion)Reflection.newInstance(Reflection.getConstructor(cbR, weworld(), vClazz, vClazz), weWorld,
                        We6Vec(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()),
                        We6Vec(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }else{
            return new CuboidRegion(
                    BukkitAdapter.asVector(Utils.LocationFromVec(bounds.Min, world)).toBlockPoint(),
                    BukkitAdapter.asVector(Utils.LocationFromVec(bounds.Max, world)).toBlockPoint()
            );
        }
    }

    public static void WeSetBlock(EditSession es, Material mat, Region region){
        DetectWe();
        if(IsWe6){
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.boydti.fawe.FaweCache");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> clazz2 = null;
            try {
                clazz2 = Class.forName("com.sk89q.worldedit.blocks.BaseBlock");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object weBlock = null;
            try {
                weBlock = Reflection.getMethod(clazz, "getBlock", int.class, int.class).invoke(null, mat.getId(), 0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            try {
                Reflection.getMethod(EditSession.class, "setBlocks", Region.class, clazz2).invoke(es, region, weBlock);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            es.setBlocks(region, BukkitAdapter.asBlockType(mat));
        }
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
        while(!stk.empty()){
            var f = stk.pop();
            for(var sub : f.listFiles()){
                CheckPath(sub);
                if(sub.isDirectory()){
                    stk.push(sub);
                }else{
                    sub.delete();
                }
            }
        }
    }
}
