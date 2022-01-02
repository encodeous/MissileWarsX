package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.mwxcompat1_13.Structures.StructureCore;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.engines.structure.StructureInterface;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.SoundType;
import com.j256.ormlite.stmt.query.In;
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
        p.sendTitle(Chat.FCL(title), Chat.FCL(subtitle), 10, 20 * 3, 10);
    }

    @Override
    public void PlaySound(Player p, SoundType type) {
        switch (type){
            case WIN:
                PlayPlayerSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE);
                break;
            case START:
                PlayPlayerSound(p, Sound.BLOCK_BEACON_ACTIVATE);
                break;
            case SHIELD:
                PlayPlayerSound(p, Sound.ITEM_SHIELD_BLOCK);
                break;
            case RESPAWN:
                PlayPlayerSound(p, Sound.BLOCK_BEACON_ACTIVATE);
                break;
            case FIREBALL:
                PlayPlayerSound(p, Sound.ITEM_FIRECHARGE_USE);
                break;
            case GAME_END:
                PlayPlayerSound(p, Sound.BLOCK_END_PORTAL_SPAWN);
                break;
            case COUNTDOWN:
                PlayPlayerSound(p, Sound.BLOCK_TRIPWIRE_CLICK_ON);
                break;
            case ITEM_GIVEN:
                PlayPlayerSound(p, Sound.ENTITY_ITEM_PICKUP);
                break;
            case ITEM_NOT_GIVEN:
                PlayPlayerSound(p, Sound.BLOCK_NOTE_BLOCK_BASS);
                break;
            case KILL_OTHER:
                PlayPlayerSound(p, Sound.ENTITY_ARROW_HIT_PLAYER);
                break;
            case KILL_TEAM:
                PlayPlayerSound(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP);
                break;
            case TELEPORT:
                PlayPlayerSound(p, Sound.ENTITY_ENDERMAN_TELEPORT);
                break;
        }
    }

    @Override
    public void PlaySound(Location p, SoundType type) {
        switch (type){
            case WIN:
                PlayWorldSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE);
                break;
            case START:
                PlayWorldSound(p, Sound.BLOCK_BEACON_ACTIVATE);
                break;
            case SHIELD:
                PlayWorldSound(p, Sound.ITEM_SHIELD_BLOCK);
                break;
            case RESPAWN:
                PlayWorldSound(p, Sound.BLOCK_BEACON_ACTIVATE);
                break;
            case FIREBALL:
                PlayWorldSound(p, Sound.ITEM_FIRECHARGE_USE);
                break;
            case GAME_END:
                PlayWorldSound(p, Sound.BLOCK_END_PORTAL_SPAWN);
                break;
            case COUNTDOWN:
                PlayWorldSound(p, Sound.BLOCK_TRIPWIRE_CLICK_ON);
                break;
            case ITEM_GIVEN:
                PlayWorldSound(p, Sound.ENTITY_ITEM_PICKUP);
                break;
            case ITEM_NOT_GIVEN:
                PlayWorldSound(p, Sound.BLOCK_NOTE_BLOCK_BASS);
                break;
            case KILL_OTHER:
                PlayWorldSound(p, Sound.ENTITY_ARROW_HIT_PLAYER);
                break;
            case KILL_TEAM:
                PlayWorldSound(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP);
                break;
            case TELEPORT:
                PlayWorldSound(p, Sound.ENTITY_ENDERMAN_TELEPORT);
                break;
        }
    }

    @Override
    public void SendActionBar(Player p, String message) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Chat.FCL(message)));
    }
    @Override
    public void RegisterEvents(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(new ca.encodeous.mwx.mwxcompat1_13.MissileWarsEventHandler(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new ca.encodeous.mwx.mwxcompat1_13.PaperEventHandler(), plugin);
    }
    @Override
    public void ConfigureScoreboards() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        ResetScoreboard(board);
        MissileWarsMatch mtch = null;
        mtch.mwGreen = GetTeam("green", board);
        mtch.mwGreen.setColor(ChatColor.GREEN);
        mtch.mwGreen.setAllowFriendlyFire(true);
        mtch.mwGreen.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        mtch.mwGreen.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwRed = GetTeam("red", board);
        mtch.mwRed.setColor(ChatColor.RED);
        mtch.mwRed.setAllowFriendlyFire(true);
        mtch.mwRed.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        mtch.mwRed.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwSpectate = GetTeam("spectator", board);
        mtch.mwSpectate.setColor(ChatColor.BLUE);
        mtch.mwSpectate.setAllowFriendlyFire(false);
        mtch.mwSpectate.setCanSeeFriendlyInvisibles(true);
        mtch.mwSpectate.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        mtch.mwSpectate.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);

        mtch.mwLobby = GetTeam("lobby", board);
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
            if(p != null) fb.setShooter(p);
            fb.setIsIncendiary(true);
            fb.setVelocity(new Vector(0, 1, 0));
            a.setPassenger(fb);
        });
        StationaryFireballTrack(a, e);
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
