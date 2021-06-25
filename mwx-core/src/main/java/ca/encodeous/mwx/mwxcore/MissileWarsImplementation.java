package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public interface MissileWarsImplementation {
    public MCVersion GetImplVersion();
    public Material GetPortalMaterial();
    public void SendTitle(Player p, String title, String subtitle);
    public void SendActionBar(Player p, String message);
    public void MakePlayerTempInvincible(Player p);
    public void EquipPlayer(Player p, boolean isRedTeam, MissileWarsItem bow);
    public void ConfigureScoreboards(MissileWarsMatch match);
    public void RegisterEvents(MissileWarsEvents events, JavaPlugin plugin);
    public void FastCloneWorld(String targetName, String sourceName);
    public MissileWarsMap CreateManualJoinMap(String name);
    public MissileWarsMap CreateAutoJoinMap(String name);
    public ItemStack CreateItem(MissileWarsItem item, boolean isRedTeam);
    public String GetItemId(ItemStack item);
    public MissileSchematic GetSchematic(Vector pivot, Bounds boundingBox, World world);
    public ArrayList<Vector> PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update);
    public void PlaceBlock(MissileBlock missile, Vector origin, World world, boolean isRed);
    public void SpawnShield(Vector location, World world, boolean isRed);
    public void SummonFrozenFireball(Vector location, World world);
    public ArrayList<MissileWarsItem> CreateDefaultItems();
}
