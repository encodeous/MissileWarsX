package ca.encodeous.mwx.mwxcompat1_8;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.mwxcompat1_8.Structures.StructureCore;
import ca.encodeous.mwx.core.game.MissileWarsMap;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.core.utils.VoidWorldGen;
import ca.encodeous.mwx.data.SoundType;
import ca.encodeous.mwx.engines.structure.StructureInterface;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class MissileWars1_8 implements MissileWarsImplementation {
    private static final StructureCore Structures = new StructureCore();

    @Override
    public MCVersion GetImplVersion() {
        return MCVersion.v1_8;
    }

    @Override
    public Material GetPortalMaterial() {
        return Material.PORTAL;
    }

    @Override
    public void SendTitle(Player p, String title, String subtitle) {
        TitleAPI.sendTitle(p, Chat.FCL(title), Chat.FCL(subtitle), 10, 20 * 5, 10);
    }
    @Override
    public void SendActionBar(Player p, String message) {
        ActionBarAPI.sendActionBar(p, Chat.FCL(message));
    }

    public ItemStack MakeArmour(Material mat, Color color){
        ItemStack hstack = new ItemStack(mat);
        LeatherArmorMeta hdata = (LeatherArmorMeta) hstack.getItemMeta();
        hdata.setColor(color);
        hdata.spigot().setUnbreakable(true);
        hdata.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        hstack.setItemMeta(hdata);
        return hstack;
    }

    @Override
    public void EquipPlayer(Player p, boolean isRedTeam) {
        Color c = isRedTeam? Color.RED : Color.LIME;
        if(CoreGame.Instance.mwConfig.UseHelmets) p.getInventory().setHelmet(MakeArmour(Material.LEATHER_HELMET, c));
        p.getInventory().setChestplate(MakeArmour(Material.LEATHER_CHESTPLATE, c));
        p.getInventory().setLeggings(MakeArmour(Material.LEATHER_LEGGINGS, c));
        p.getInventory().setBoots(MakeArmour(Material.LEATHER_BOOTS, c));
        p.getInventory().addItem(CreateItem(CoreGame.Instance.GetItemById(MissileWarsCoreItem.GUNBLADE.getValue())));
    }

    public void ConfigureScoreboards() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        ResetScoreboard(board);
        MissileWarsMatch mtch = null;
        mtch.mwGreen = board.registerNewTeam("green");
        mtch.mwGreen.setPrefix("§a");
        mtch.mwGreen.setNameTagVisibility(NameTagVisibility.ALWAYS);

        mtch.mwRed = board.registerNewTeam("red");
        mtch.mwRed.setPrefix("§c");
        mtch.mwRed.setNameTagVisibility(NameTagVisibility.ALWAYS);

        mtch.mwSpectate = board.registerNewTeam("spectator");
        mtch.mwSpectate.setPrefix("§9");
        mtch.mwSpectate.setAllowFriendlyFire(false);
        mtch.mwSpectate.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        mtch.mwSpectate.setCanSeeFriendlyInvisibles(true);

        mtch.mwLobby = board.registerNewTeam("lobby");
        mtch.mwLobby.setPrefix("§7");
        mtch.mwLobby.setAllowFriendlyFire(false);
        mtch.mwLobby.setNameTagVisibility(NameTagVisibility.ALWAYS);
    }

    public void ResetScoreboard(Scoreboard scoreboard){
        for(String x : scoreboard.getEntries()){
            scoreboard.resetScores(x);
        }
        for(Objective x : scoreboard.getObjectives()){
            x.unregister();
        }
        for(Team x : scoreboard.getTeams()){
            x.unregister();
        }
    }

    @Override
    public void RegisterEvents(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(new MissileWarsEventHandler(), plugin);
    }

    @Override
    public World FastVoidWorld(String targetName) {
        WorldCreator wc = new WorldCreator(targetName);
        wc.type(WorldType.FLAT);
        wc.environment(World.Environment.NORMAL);
        wc.generator(new VoidWorldGen());
        wc.seed(0);
        wc.generateStructures(false);
        World world = wc.createWorld();
        ConfigureWorld(world);
        return world;
    }

    @Override
    public void ConfigureWorld(World world) {
        world.setAutoSave(false);
        world.setTicksPerAnimalSpawns(1000000000);
        world.setTicksPerMonsterSpawns(1000000000);
        world.setAmbientSpawnLimit(0);
        world.setWaterAnimalSpawnLimit(0);
        world.setAnimalSpawnLimit(0);
        world.setTime(6000);
        world.setStorm(false);
        world.setWeatherDuration(1000000000);
        world.setDifficulty(Difficulty.EASY);
        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(500);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("doTileDrops", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_14.getValue()){
            world.setGameRuleValue("disableRaids", "true");
        }
        if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_15.getValue()){
            world.setGameRuleValue("doImmediateRespawn", "true");
        }
        world.setGameRuleValue("announceAdvancements", "false");
    }


    @Override
    public MissileWarsMap CreateManualJoinMap(String name) {
        MissileWarsMap map = new MissileWarsMap();
        map.WorldName = name;
        map.SeparateJoin = true;
        map.RedJoin = new HashSet<>(Arrays.asList(new Vector(-118,65,-6), new Vector(-118,65,-7), new Vector(-118,65,-8), new Vector(-118,65,-9)));
        map.GreenJoin = new HashSet<>(Arrays.asList(new Vector(-118,65,9), new Vector(-118,65,8), new Vector(-118,65,7), new Vector(-118,65,6)));
        map.TemplateWorld = CoreGame.Instance.mwManual;
        return getMissileWarsMap(name, map);
    }

    @Override
    public MissileWarsMap CreateAutoJoinMap(String name) {
        MissileWarsMap map = new MissileWarsMap();
        map.WorldName = name;
        map.SeparateJoin = false;
        map.AutoJoin = new HashSet<>(Arrays.asList(new Vector(-115,66,2), new Vector(-115,66,1), new Vector(-115,66,0), new Vector(-115,66,-1), new Vector(-115,66,-2)));
        map.TemplateWorld = CoreGame.Instance.mwAuto;
        return getMissileWarsMap(name, map);
    }

    private MissileWarsMap getMissileWarsMap(String name, MissileWarsMap map) {
        map.ReturnToLobby = new HashSet<>(Arrays.asList(new Vector(-85,79,19), new Vector(-85,79,-19)));
        map.Spectate = new HashSet<>(Arrays.asList(new Vector(-91, 69, 0), new Vector(-79, 78, 0)));
        map.Spawn = new Vector(-100.5, 70, 0.5);
        map.GreenLobby = new Vector(-82.5, 78, 21.5);
        map.RedLobby = new Vector(-82.5, 78, -20.5);
        map.RedSpawn = new Vector(-26.5, 77, -64.5);
        map.GreenSpawn = new Vector(-26.5, 77, 65.5);
        map.MswWorld = Bukkit.getWorld(name);
        map.RedPortal = Bounds.of(new Vector(-48, 72, -72), new Vector(-6, 52, -72));
        map.GreenPortal = Bounds.of(new Vector(-48, 72, 72), new Vector(-6, 52, 72));
        map.WorldBoundingBox = Bounds.of(new Vector(1, 0, -95), new Vector(-140, 255, 95));
        map.WorldMaxBoundingBox = Bounds.of(new Vector(250, 0, 250), new Vector(-350, 255, -250));
        map.SpawnYaw = 90;
        map.GreenYaw = -180;
        map.RedYaw = 0;
        return map;
    }

    @Override
    public String GetItemId(ItemStack item) {
        if(item == null) return "";
        if(item.getType() == Material.ARROW) return MissileWarsCoreItem.ARROW.getValue();
        if(item.hasItemMeta()){
            if(!item.getItemMeta().hasLore()) return "";
            List<String> s = item.getItemMeta().getLore();
            if(s != null && !s.isEmpty()){
                String lore = s.get(s.size()-1);
                if(lore.startsWith(Chat.FCL("&0msw-internal:"))){
                    return lore.substring(15);
                }
            }
        }
        return "";
    }

    @Override
    public void SummonFrozenFireball(Vector location, World world, Player p) {
        ArmorStand a = world.spawn(Utils.LocationFromVec(location.add(new Vector(0,-1.5,0)), world), ArmorStand.class);
        a.setVisible(false);
        a.setGravity(false);
        a.setMarker(true);
        Fireball e = world.spawn(Utils.LocationFromVec(location.add(new Vector(0,-1.5,0)), world), Fireball.class);
        e.setYield(1.5f);
        e.setShooter(p);
        e.setIsIncendiary(true);
        e.setVelocity(new Vector(0, 1, 0));
        a.setPassenger(e);
        StationaryFireballTrack(a, e);
    }

    public void StationaryFireballTrack(ArmorStand a, Fireball e) {
        if(CoreGame.Instance.mwConfig.StationaryFireballExplode){
            Ref<Integer> tid = new Ref<>(-1);
            tid.val = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoreGame.Instance.mwPlugin, () -> {
                if (e.isDead() || a.isDead() || Objects.equals(a.getCustomName(), "removed")) {
                    if(tid.val != -1){
                        Bukkit.getScheduler().cancelTask(tid.val);
                    }
                    a.remove();
                } else {
                    if(e.getLocation().getBlock().getType().isSolid()){
                        e.leaveVehicle();
                    }
                }
            }, 2, 1);
        }
    }

    @Override
    public ArrayList<MissileWarsItem> CreateDefaultItems() {
        return MwConstants.CreateDefaultItems();
    }

    @Override
    public ItemStack CreateItem(MissileWarsItem item) {
        return MwUtils.CreateItem(item);
    }

    @Override
    public void PlaySound(Player p, SoundType type) {
        switch (type){
            case WIN:
                PlayPlayerSound(p, Sound.LEVEL_UP);
                break;
            case START:
                PlayPlayerSound(p, Sound.ORB_PICKUP);
                break;
            case SHIELD:
                PlayPlayerSound(p, Sound.ITEM_BREAK);
                break;
            case RESPAWN:
                PlayPlayerSound(p, Sound.CLICK);
                break;
            case FIREBALL:
                PlayPlayerSound(p, Sound.GHAST_FIREBALL);
                break;
            case GAME_END:
                PlayPlayerSound(p, Sound.WITHER_DEATH);
                break;
            case COUNTDOWN:
                PlayPlayerSound(p, Sound.CLICK);
                break;
            case ITEM_GIVEN:
                PlayPlayerSound(p, Sound.ITEM_PICKUP);
                break;
            case ITEM_NOT_GIVEN:
                PlayPlayerSound(p, Sound.NOTE_PLING);
                break;
            case KILL_OTHER:
                PlayPlayerSound(p, Sound.ARROW_HIT);
                break;
            case KILL_TEAM:
                PlayPlayerSound(p, Sound.NOTE_STICKS);
                break;
            case TELEPORT:
                PlayPlayerSound(p, Sound.ENDERMAN_TELEPORT);
                break;
        }
    }

    @Override
    public void PlaySound(Location p, SoundType type) {
        switch (type){
            case WIN:
                PlayWorldSound(p, Sound.LEVEL_UP);
                break;
            case START:
                PlayWorldSound(p, Sound.ORB_PICKUP);
                break;
            case SHIELD:
                PlayWorldSound(p, Sound.ITEM_BREAK);
                break;
            case RESPAWN:
                PlayWorldSound(p, Sound.CLICK);
                break;
            case FIREBALL:
                PlayWorldSound(p, Sound.GHAST_FIREBALL);
                break;
            case GAME_END:
                PlayWorldSound(p, Sound.WITHER_DEATH);
                break;
            case COUNTDOWN:
                PlayWorldSound(p, Sound.CLICK);
                break;
            case ITEM_GIVEN:
                PlayWorldSound(p, Sound.ITEM_PICKUP);
                break;
            case ITEM_NOT_GIVEN:
                PlayWorldSound(p, Sound.NOTE_PLING);
                break;
            case KILL_OTHER:
                PlayWorldSound(p, Sound.ARROW_HIT);
                break;
            case KILL_TEAM:
                PlayWorldSound(p, Sound.NOTE_STICKS);
                break;
            case TELEPORT:
                PlayWorldSound(p, Sound.ENDERMAN_TELEPORT);
                break;
        }
    }

    public void PlayPlayerSound(Player p, Sound sound){
        p.playSound(p.getLocation(), sound, 1, 1);
    }

    public void PlayWorldSound(Location loc, Sound sound){

        loc.getWorld().playSound(loc, sound, 1, 0);
    }

    @Override
    public StructureInterface GetStructureManager() {
        return Structures;
    }
}
