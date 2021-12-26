package ca.encodeous.mwx.mwxcompat1_8.Structures;

import ca.encodeous.mwx.configuration.*;
import ca.encodeous.mwx.mwxcompat1_8.MwConstants;
import ca.encodeous.mwx.engines.structure.StructureInterface;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.data.TraceType;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.engines.structure.StructureUtils;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.MissileMaterial;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StructureCore implements StructureInterface {
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
                    if (block.getType() == Material.PISTON_BASE) {
                        mBlock.Material = MissileMaterial.PISTON;
                        PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.PistonData = new PistonBlock();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = false;
                        mBlock.PistonData.IsPowered = pbm.isPowered();
                        mBlock.PistonData.Face = pbm.getFacing();
                    } else if (block.getType() == Material.PISTON_STICKY_BASE) {
                        mBlock.Material = MissileMaterial.PISTON;
                        PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.PistonData = new PistonBlock();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = true;
                        mBlock.PistonData.IsPowered = pbm.isPowered();
                        mBlock.PistonData.Face = pbm.getFacing();
                    } else if (block.getType() == Material.PISTON_EXTENSION) {
                        PistonExtensionMaterial pem = new PistonExtensionMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.Material = MissileMaterial.PISTON;
                        mBlock.PistonData = new PistonBlock();
                        mBlock.PistonData.IsHead = true;
                        mBlock.PistonData.IsSticky = pem.isSticky();
                        mBlock.PistonData.Face = pem.getAttachedFace();
                    } else if (block.getType() == Material.SLIME_BLOCK) {
                        mBlock.Material = MissileMaterial.SLIME;
                    } else if (block.getType() == Material.STAINED_GLASS) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.GLASS) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.TNT) {
                        mBlock.Material = MissileMaterial.TNT;
                    } else if (block.getType() == Material.REDSTONE_BLOCK) {
                        mBlock.Material = MissileMaterial.REDSTONE;
                    } else if (block.getType() == Material.STAINED_CLAY) {
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
    @Override
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p) {
        Bounds box = GetPlacementBounds(missile, location, world, isRed);
        if (box == null) return false;
        PlaceTNT(missile, location, world, isRed, p, update);
        if(update) UpdateMissileBounds(box, world);
        PlaceComponents(missile, location, world, isRed, p, update);
        return true;
    }

    protected void UpdateMissileBounds(Bounds box, World world){
        for(int i = box.getMinX(); i <= box.getMaxX(); i++){
            for(int j = box.getMinY(); j <= box.getMaxY(); j++){
                for(int k = box.getMinZ(); k <= box.getMaxZ(); k++) {
                    Block block = world.getBlockAt(i, j, k);
                    Material originalType = block.getType();
                    if(originalType == Material.SLIME_BLOCK || originalType == Material.REDSTONE_BLOCK){
                        byte data = block.getData();
                        block.setType(Material.STAINED_GLASS);
                        block.setType(originalType);
                        block.setData(data, true);
                    }
                }
            }
        }
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
        UpdateBlocks(redstoneBlocks, Material.REDSTONE_BLOCK, world, p, false);
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
    public Material PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p) {
        Vector location = origin.clone().add(block.Location);
        Block realBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Material mat = realBlock.getType();
        if(block.Material == MissileMaterial.PISTON){
            if(block.PistonData.IsHead){
                PistonExtensionMaterial pem = new PistonExtensionMaterial(Material.PISTON_EXTENSION);
                pem.setFacingDirection(block.PistonData.Face);
                pem.setSticky(block.PistonData.IsSticky);
                realBlock.setType(Material.PISTON_EXTENSION, false);
                realBlock.setData(pem.getData(), false);
            }else{
                if(block.PistonData.IsSticky){
                    PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_STICKY_BASE);
                    pbm.setFacingDirection(block.PistonData.Face);
                    pbm.setPowered(block.PistonData.IsPowered);
                    realBlock.setType(Material.PISTON_STICKY_BASE, false);
                    realBlock.setData(pbm.getData(), false);
                }else{
                    PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE);
                    pbm.setFacingDirection(block.PistonData.Face);
                    pbm.setPowered(block.PistonData.IsPowered);
                    realBlock.setType(Material.PISTON_BASE, false);
                    realBlock.setData(pbm.getData(), false);
                }
            }
        }else if(block.Material == MissileMaterial.SLIME){
            realBlock.setType(Material.SLIME_BLOCK, false);
        }else if(block.Material == MissileMaterial.GLASS){
            if(isRed){
                realBlock.setType(Material.STAINED_GLASS, false);
                realBlock.setData(DyeColor.RED.getData(), false);
            }else{
                realBlock.setType(Material.STAINED_GLASS, false);
                realBlock.setData(DyeColor.GREEN.getData(), false);
            }
        }else if(block.Material == MissileMaterial.CLAY){
            if(isRed){
                realBlock.setType(Material.STAINED_CLAY, false);
                realBlock.setData(DyeColor.RED.getData(), false);
            }else{
                realBlock.setType(Material.STAINED_CLAY, false);
                realBlock.setData(DyeColor.GREEN.getData(), false);
            }
        } else PlaceCompatibleBlocks(block, world, p, location, realBlock);
        return mat;
    }

    protected void PlaceCompatibleBlocks(MissileBlock block, World world, Player p, Vector location, Block realBlock) {
        if(block.Material == MissileMaterial.TNT){
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
    public boolean SpawnShield(Vector location, World world, boolean isRed) {
        Map<Vector, Integer> shield = MwConstants.ShieldData(isRed);
        ArrayList<Vector> realLocation = new ArrayList<>();
        for(Vector key : shield.keySet()){
            realLocation.add(location.clone().add(key));
        }
        if(!StructureUtils.CheckCanSpawn(isRed ?
                PlayerTeam.Red : PlayerTeam.Green, realLocation, world, true)) return false;
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            if(e.getValue() == 1){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.PINK.getData(), false);
            }
            if(e.getValue() == 2){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.WHITE.getData(), false);
            }
            if(e.getValue() == 3){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.RED.getData(), false);
            }
            if(e.getValue() == 4){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.SILVER.getData(), false);
            }
            if(e.getValue() == 5){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.GRAY.getData(), false);
            }
            if(e.getValue() == 6){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.BLACK.getData(), false);
            }
            if(e.getValue() == 7){
                block.setType(Material.STAINED_GLASS_PANE, false);
                block.setData(DyeColor.BLACK.getData(), false);
            }
            if(e.getValue() == 8){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.LIME.getData(), false);
            }
            if(e.getValue() == 9){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.GREEN.getData(), false);
            }
        }
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            byte data = block.getData();
            Material mat = block.getType();
            block.setType(mat, true);
            block.setData(data, true);
        }
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            if(block.getType() == Material.STAINED_GLASS_PANE) continue;
            byte data = block.getData();
            Material mat = block.getType();
            block.setType(Material.STONE, true);
            block.setType(mat, true);
            block.setData(data, true);
        }
        return true;
    }
    @Override
    public boolean IsBlockOfTeam(PlayerTeam team, Block block) {
        if (block.getType() == Material.STAINED_GLASS){
            DyeColor color = DyeColor.getByData(block.getData());
            if(team == PlayerTeam.Green){
                return color == DyeColor.GREEN || color == DyeColor.LIME;
            }else if(team == PlayerTeam.Red){
                return color == DyeColor.RED || color == DyeColor.PINK;
            }
            else if(team == PlayerTeam.None){
                return color == DyeColor.WHITE;
            }
        }
        return false;
    }
}
