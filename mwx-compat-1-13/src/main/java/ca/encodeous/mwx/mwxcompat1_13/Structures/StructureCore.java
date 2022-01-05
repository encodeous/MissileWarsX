package ca.encodeous.mwx.mwxcompat1_13.Structures;

import ca.encodeous.mwx.configuration.*;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.data.TraceType;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.engines.structure.StructureInterface;
import ca.encodeous.mwx.engines.structure.StructureUtils;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.MissileMaterial;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
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
import java.util.List;
import java.util.Map;

import static ca.encodeous.mwx.mwxcompat1_13.MwConstants.ShieldData;

public class StructureCore implements StructureInterface {

    protected void UpdateMissileBounds(Bounds box, World world){
        for(int i = box.getMinX() - 1; i <= box.getMaxX() + 1; i++){
            for(int j = box.getMinY() - 1; j <= box.getMaxY() + 1; j++){
                for(int k = box.getMinZ() - 1; k <= box.getMaxZ() + 1; k++) {
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

    @Override
    public Material PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p) {
        Vector location = origin.clone().add(block.Location);
        Block realBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Material mat = realBlock.getType();
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
        }else PlaceCompatibleBlocks(block, world, p, location, realBlock);
        return mat;
    }

    protected void PlaceCompatibleBlocks(MissileBlock block, World world, Player p, Vector location, Block realBlock) {
        if(block.Material == MissileMaterial.TNT){
            realBlock.setType(Material.TNT, false);
            MissileWarsMatch match = LobbyEngine.FromWorld(world);
            if(match != null && p != null){
                match.Tracer.AddBlock(p.getUniqueId(), TraceType.TNT, location);
            }
        }else if(block.Material == MissileMaterial.REDSTONE){
            realBlock.setType(Material.REDSTONE_BLOCK, false);
            MissileWarsMatch match = LobbyEngine.FromWorld(world);
            if(match != null && p != null){
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

    protected Bounds GetPlacementBounds(Missile missile, Vector location, World world, boolean isRed) {
        List<MissileBlock> blocks;
        if(isRed){
            blocks = missile.Schematic.Blocks;
        }else{
            blocks = missile.Schematic.CreateOppositeSchematic().Blocks;
        }
        Bounds box = new Bounds();
        ArrayList<Vector> placedBlocks = new ArrayList<>();
        for(MissileBlock block : blocks){
            placedBlocks.add(location.clone().add(block.Location));
        }
        if(!StructureUtils.CheckCanSpawn(isRed ? PlayerTeam.Red : PlayerTeam.Green, placedBlocks, world, false))
            return null;
        for(MissileBlock block : blocks){
            box.stretch(location.clone().add(block.Location));
        }
        return box;
    }

    protected void PlaceTNT(Missile missile, Vector location, World world, boolean isRed, Player p, boolean update) {
        List<MissileBlock> blocks;
        if(isRed){
            blocks = missile.Schematic.Blocks;
        }else{
            blocks = missile.Schematic.CreateOppositeSchematic().Blocks;
        }
        ArrayList<Block> redstoneBlocks = new ArrayList<>();
        for(MissileBlock block : blocks){
            if(block.Material == MissileMaterial.TNT){
                Vector loc = location.clone().add(block.Location);
                Block realBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                if(update && realBlock.getType() == Material.REDSTONE_BLOCK){
                    redstoneBlocks.add(realBlock);
                }else{
                    PlaceBlock(block, location, world, isRed, p);
                }
            }
        }
    }

    protected void PlaceComponents(Missile missile, Vector location, World world, boolean isRed, Player p, boolean update) {
        List<MissileBlock> blocks;
        if(isRed){
            blocks = missile.Schematic.Blocks;
        }else{
            blocks = missile.Schematic.CreateOppositeSchematic().Blocks;
        }
        ArrayList<Block> redstoneBlocks = new ArrayList<>();
        ArrayList<Block> slimeBlocks = new ArrayList<>();
        for(MissileBlock block : blocks){
            Vector loc = location.clone().add(block.Location);
            if(update){
                if(block.Material == MissileMaterial.REDSTONE){
                    Block realBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    redstoneBlocks.add(realBlock);
                }else if(block.Material == MissileMaterial.SLIME){
                    Block realBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    slimeBlocks.add(realBlock);
                }
            }
            if(block.Material != MissileMaterial.TNT){
                PlaceBlock(block, location, world, isRed, p);
            }
        }
        UpdateBlocks(redstoneBlocks, Material.REDSTONE_BLOCK, world, p, true);
        UpdateBlocks(slimeBlocks, Material.SLIME_BLOCK, world, p, true);
    }

    protected void UpdateBlocks(ArrayList<Block> blocks, Material rep, World w, Player p, boolean refill){
        MissileWarsMatch match = LobbyEngine.FromWorld(w);
        for(Block realBlock : blocks){
            if(refill){
                realBlock.setType(Material.STONE);
            }
            realBlock.setType(rep, true);
            if(rep == Material.TNT){
                match.Tracer.AddBlock(p.getUniqueId(), TraceType.TNT, realBlock.getLocation().toVector());
            }
        }
    }

    @Override
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p) {
        Bounds box = GetPlacementBounds(missile, location, world, isRed);
        if (box == null) return false;
        PlaceTNT(missile, location, world, isRed, p, update);
        if(update) UpdateMissileBounds(box, world);
        PlaceComponents(missile, location, world, isRed, p, update);
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
                        mBlock.PistonData = new PistonBlock();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = false;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.STICKY_PISTON) {
                        mBlock.Material = MissileMaterial.PISTON;
                        Piston piston = (Piston) block.getBlockData();
                        mBlock.PistonData = new PistonBlock();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = true;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.PISTON_HEAD) {
                        PistonHead pistonHead = (PistonHead) block.getBlockData();
                        mBlock.Material = MissileMaterial.PISTON;
                        mBlock.PistonData = new PistonBlock();
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

    public boolean IsGlassBlock(Block block){
        return block.getType().toString().endsWith("STAINED_GLASS") || block.getType().toString().endsWith("STAINED_GLASS_PANE");
    }

    @Override
    public boolean IsNeutralBlock(Block block) {
        return !IsBlockOfTeam(PlayerTeam.Green, block) && !IsBlockOfTeam(PlayerTeam.Red, block) && IsGlassBlock(block);
    }
}
