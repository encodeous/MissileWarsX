package ca.encodeous.mwx.engines.structure;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.configuration.MissileBlock;
import ca.encodeous.mwx.configuration.MissileSchematic;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface StructureInterface {
    public MissileSchematic GetSchematic(Vector pivot, Bounds boundingBox, World world);
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p);
    public Material PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p);
    public boolean SpawnShield(Vector location, World world, boolean isRed);
    public boolean IsBlockOfTeam(PlayerTeam team, Block block);
    public boolean IsNeutralBlock(Block block);
}
