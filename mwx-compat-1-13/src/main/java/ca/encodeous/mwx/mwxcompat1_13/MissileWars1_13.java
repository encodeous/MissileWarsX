package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcompat1_13.Structures.StructureCore;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.StructureInterface;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

import static ca.encodeous.mwx.mwxcompat1_8.MwConstants.ShieldData;

public class MissileWars1_13 extends ca.encodeous.mwx.mwxcompat1_8.MissileWars1_8 {

    private static final StructureCore Structures = new StructureCore();
    @Override
    public MCVersion GetImplVersion() {
        return MCVersion.v1_13;
    }

    @Override
    public Material GetPortalMaterial() {
        return Material.NETHER_PORTAL;
    }

    @Override
    public void SendTitle(Player p, String title, String subtitle) {
        p.sendTitle(Formatter.FCL(title), Formatter.FCL(subtitle), 10, 20 * 5, 10);
    }

    @Override
    public void SendActionBar(Player p, String message) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Formatter.FCL(message)));
    }
    @Override
    public void RegisterEvents(MissileWarsEvents events, JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(new ca.encodeous.mwx.mwxcompat1_13.MissileWarsEventHandler(events), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new ca.encodeous.mwx.mwxcompat1_13.PaperEventHandler(), plugin);
    }
    @Override
    public void ConfigureScoreboards(MissileWarsMatch mtch) {
        mtch.mwScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        mtch.mwGreen = GetTeam("green", mtch.mwScoreboard);
        mtch.mwGreen.setColor(ChatColor.GREEN);
        mtch.mwGreen.setAllowFriendlyFire(true);
        mtch.mwGreen.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        mtch.mwGreen.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwRed = GetTeam("red", mtch.mwScoreboard);
        mtch.mwRed.setColor(ChatColor.RED);
        mtch.mwRed.setAllowFriendlyFire(true);
        mtch.mwRed.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        mtch.mwRed.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwSpectate = GetTeam("spectator", mtch.mwScoreboard);
        mtch.mwSpectate.setColor(ChatColor.BLUE);
        mtch.mwSpectate.setAllowFriendlyFire(false);
        mtch.mwSpectate.setCanSeeFriendlyInvisibles(true);
        mtch.mwSpectate.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        mtch.mwSpectate.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwLobby = GetTeam("lobby", mtch.mwScoreboard);
        mtch.mwLobby.setColor(ChatColor.GRAY);
        mtch.mwLobby.setAllowFriendlyFire(false);
        mtch.mwLobby.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        mtch.mwLobby.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
    }

    public Team GetTeam(String team, Scoreboard board){
        if(board.getTeam(team) == null) return board.registerNewTeam(team);
        else return board.getTeam(team);
    }

    public ItemStack MakeArmour(Material mat, Color color){
        ItemStack hstack = new ItemStack(mat);
        LeatherArmorMeta hdata = (LeatherArmorMeta) hstack.getItemMeta();
        hdata.setColor(color);
        hdata.setUnbreakable(true);
        hdata.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        hstack.setItemMeta(hdata);
        return hstack;
    }

    @Override
    public void SummonFrozenFireball(Vector location, World world, Player p) {
        ArmorStand a = world.spawn(Utils.LocationFromVec(location, world), ArmorStand.class, stand->{
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
        });
        Fireball e = world.spawn(Utils.LocationFromVec(location, world), Fireball.class, fb->{
            fb.setYield(1.5f);
            fb.setShooter(p);
            fb.setIsIncendiary(true);
            fb.setVelocity(new Vector(0, 1, 0));
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, () -> {
            if (e.isDead()) {
                a.remove();
            } else {
                a.setPassenger(e);
            }
        }, 2);
    }

    @Override
    public ArrayList<MissileWarsItem> CreateDefaultItems() {
        return MwConstants.CreateDefaultItems();
    }

    @Override
    public StructureInterface GetStructureManager() {
        return Structures;
    }
}
