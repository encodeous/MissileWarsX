package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import ca.encodeous.mwx.soundengine.SoundType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public interface MissileWarsImplementation {
    public MCVersion GetImplVersion();
    public Material GetPortalMaterial();
    public void SendTitle(Player p, String title, String subtitle);
    public void SendActionBar(Player p, String message);
    public void EquipPlayer(Player p, boolean isRedTeam);
    public void ConfigureScoreboards();
    public void RegisterEvents(JavaPlugin plugin);
    public World FastVoidWorld(String targetName);
    public void ConfigureWorld(World world);
    public MissileWarsMap CreateManualJoinMap(String name);
    public MissileWarsMap CreateAutoJoinMap(String name);
    public String GetItemId(ItemStack item);
    public void SummonFrozenFireball(Vector location, World world, Player p);
    public ArrayList<MissileWarsItem> CreateDefaultItems();
    public ItemStack CreateItem(MissileWarsItem item);
    public void PlaySound(Player p, SoundType type);
    public void PlaySound(Location loc, SoundType type);
    // Structures
    public StructureInterface GetStructureManager();
}
