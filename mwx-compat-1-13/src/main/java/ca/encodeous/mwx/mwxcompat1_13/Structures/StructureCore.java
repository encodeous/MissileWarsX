package ca.encodeous.mwx.mwxcompat1_13.Structures;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.trace.TraceType;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.utils.StructureUtils;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileMaterial;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import ca.encodeous.mwx.mwxcore.world.PistonData;
import ca.encodeous.mwx.lobbyengine.LobbyEngine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;

import static ca.encodeous.mwx.mwxcompat1_8.MwConstants.ShieldData;

public class StructureCore extends ca.encodeous.mwx.mwxcompat1_8.Structures.StructureCore {
    @Override
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p) {
        Bounds box = PreProcessMissilePlacement(missile, location, world, isRed, p);
        if (box == null) return false;
        if(update){
            for(int i = box.getMinX(); i <= box.getMaxX(); i++){
                for(int j = box.getMinY(); j <= box.getMaxY(); j++){
                    for(int k = box.getMinZ(); k <= box.getMaxZ(); k++) {
                        Block block = world.getBlockAt(i, j, k);
                        Material originalType = block.getType();
                        if(originalType == Material.SLIME_BLOCK || originalType == Material.REDSTONE_BLOCK){
                            BlockData data = block.getBlockData();
                            block.setType(Material.WHITE_STAINED_GLASS);
                            block.setType(originalType);
                            block.setBlockData(data, true);
                        }
                    }
                }
            }
        }
        return true;
    }
    @Override
    public void PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p) {
        Vector location = origin.clone().add(block.Location);
        Block realBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if(block.Material == MissileMaterial.PISTON){
            if(block.PistonData.IsHead){
                realBlock.setType(Material.PISTON_HEAD, false);
                PistonHead piston = (PistonHead) realBlock.getBlockData();
                piston.setFacing(block.PistonData.Face);
                piston.setType(block.PistonData.IsSticky ? TechnicalPiston.Type.STICKY : TechnicalPiston.Type.NORMAL);
                realBlock.setBlockData(piston, false);
            }else{
                realBlock.setType(block.PistonData.IsSticky ? Material.STICKY_PISTON : Material.PISTON, false);
                Piston piston = (Piston) realBlock.getBlockData();
                piston.setFacing(block.PistonData.Face);
                piston.setExtended(block.PistonData.IsPowered);
                piston.setFacing(block.PistonData.Face);
                realBlock.setBlockData(piston, false);
            }
        }else if(block.Material == MissileMaterial.SLIME){
            realBlock.setType(Material.SLIME_BLOCK, false);
        }else if(block.Material == MissileMaterial.GLASS){
            if(isRed){
                realBlock.setType(Material.RED_STAINED_GLASS, false);
            }else{
                realBlock.setType(Material.GREEN_STAINED_GLASS, false);
            }
        }else if(block.Material == MissileMaterial.CLAY){
            if(isRed){
                realBlock.setType(Material.RED_TERRACOTTA, false);
            }else{
                realBlock.setType(Material.GREEN_TERRACOTTA, false);
            }
        }else if(block.Material == MissileMaterial.TNT){
            realBlock.setType(Material.TNT, false);
            MissileWarsMatch match = LobbyEngine.FromWorld(world);
            if(match != null){
                match.Tracer.AddBlock(p.getUniqueId(), TraceType.TNT, location);
            }
        }else if(block.Material == MissileMaterial.REDSTONE){
            realBlock.setType(Material.REDSTONE_BLOCK, false);
            MissileWarsMatch match = LobbyEngine.FromWorld(world);
            if(match != null){
                match.Tracer.AddBlock(p.getUniqueId(), TraceType.REDSTONE, location);
            }
        }
    }

    @Override
    public boolean IsBlockOfTeam(PlayerTeam team, Block block) {
        Material mat = block.getType();
        if(team == PlayerTeam.Green){
            return mat == Material.GREEN_STAINED_GLASS || mat == Material.LIME_STAINED_GLASS;
        }else if(team == PlayerTeam.Red){
            return mat == Material.RED_STAINED_GLASS || mat == Material.PINK_STAINED_GLASS;
        }
        else if(team == PlayerTeam.None){
            return mat == Material.WHITE_STAINED_GLASS;
        }
        return false;
    }
    @Override
    public boolean SpawnShield(Vector location, World world, boolean isRed) {
        Map<Vector, Integer> shield = ShieldData(isRed);
        ArrayList<Vector> realLocation = new ArrayList<>();
        for(Vector key : shield.keySet()){
            realLocation.add(location.clone().add(key));
        }
        if(!StructureUtils.CheckCanSpawn(isRed ?
                PlayerTeam.Red : PlayerTeam.Green, realLocation, world, true)) return false;
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            if(e.getValue() == 1){
                block.setType(Material.PINK_STAINED_GLASS, true);
            }
            if(e.getValue() == 2){
                block.setType(Material.WHITE_STAINED_GLASS, true);
            }
            if(e.getValue() == 3){
                block.setType(Material.RED_STAINED_GLASS, true);
            }
            if(e.getValue() == 4){
                block.setType(Material.LIGHT_GRAY_STAINED_GLASS, true);
            }
            if(e.getValue() == 5){
                block.setType(Material.GRAY_STAINED_GLASS, true);
            }
            if(e.getValue() == 6){
                block.setType(Material.BLACK_STAINED_GLASS, true);
            }
            if(e.getValue() == 7){
                block.setType(Material.BLACK_STAINED_GLASS_PANE, true);
            }
            if(e.getValue() == 8){
                block.setType(Material.LIME_STAINED_GLASS, true);
            }
            if(e.getValue() == 9){
                block.setType(Material.GREEN_STAINED_GLASS, true);
            }
        }
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            if(block.getType() == Material.BLACK_STAINED_GLASS_PANE) continue;
            Material mat = block.getType();
            block.setType(Material.STONE, true);
            block.setType(mat, true);
        }
        return true;
    }
    @Override
    public MissileSchematic GetSchematic(Vector pivot, Bounds boundingBox, World world) {
        MissileSchematic schematic = new MissileSchematic();
        schematic.Blocks = new ArrayList<>();
        for(int i = boundingBox.getMinX(); i <= boundingBox.getMaxX(); i++){
            for(int j = boundingBox.getMinY(); j <= boundingBox.getMaxY(); j++){
                for(int k = boundingBox.getMinZ(); k <= boundingBox.getMaxZ(); k++) {
                    Block block = world.getBlockAt(i, j, k);
                    if (block.getType() == Material.AIR) continue;
                    MissileBlock mBlock = new MissileBlock();
                    mBlock.Location = new Vector(i,j,k).subtract(pivot);
                    if (block.getType() == Material.PISTON) {
                        mBlock.Material = MissileMaterial.PISTON;
                        Piston piston = (Piston) block.getBlockData();
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = false;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.STICKY_PISTON) {
                        mBlock.Material = MissileMaterial.PISTON;
                        Piston piston = (Piston) block.getBlockData();
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = true;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.PISTON_HEAD) {
                        PistonHead pistonHead = (PistonHead) block.getBlockData();
                        mBlock.Material = MissileMaterial.PISTON;
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = true;
                        mBlock.PistonData.IsSticky = pistonHead.getType() == TechnicalPiston.Type.STICKY;
                        mBlock.PistonData.Face = pistonHead.getFacing();
                    } else if (block.getType() == Material.SLIME_BLOCK) {
                        mBlock.Material = MissileMaterial.SLIME;
                    } else if (block.getType().toString().contains("STAINED_GLASS")) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.GLASS) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.TNT) {
                        mBlock.Material = MissileMaterial.TNT;
                    } else if (block.getType() == Material.REDSTONE_BLOCK) {
                        mBlock.Material = MissileMaterial.REDSTONE;
                    } else if (block.getType().toString().contains("TERRACOTTA")) {
                        mBlock.Material = MissileMaterial.CLAY;
                    } else {
                        return null;
                    }
                    schematic.Blocks.add(mBlock);
                }
            }
        }
        if(schematic.Blocks.isEmpty()) return null;
        return schematic;
    }
}
