package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface StructureInterface {
    public MissileSchematic GetSchematic(Vector pivot, Bounds boundingBox, World world);
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p);
    public void PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p);
    public boolean SpawnShield(Vector location, World world, boolean isRed);
    public boolean IsBlockOfTeam(PlayerTeam team, Block block);
}
