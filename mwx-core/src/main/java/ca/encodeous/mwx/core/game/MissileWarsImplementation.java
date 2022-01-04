package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.data.SoundType;
import ca.encodeous.mwx.engines.command.CommandBase;
import ca.encodeous.mwx.engines.structure.StructureInterface;
import org.bukkit.Location;
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
    // Commands
    public CommandBase GetCommandCore();
}
