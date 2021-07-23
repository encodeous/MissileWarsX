package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceType;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import ca.encodeous.mwx.mwxcore.world.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class MissileWars1_13 extends ca.encodeous.mwx.mwxcompat1_8.MissileWars1_8 {
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
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = false;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.STICKY_PISTON) {
                        mBlock.Material = MissileMaterial.PISTON;
                        Piston piston = (Piston) block.getBlockData();
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = true;
                        mBlock.PistonData.IsPowered = piston.isExtended();
                        mBlock.PistonData.Face = piston.getFacing();
                    } else if (block.getType() == Material.PISTON_HEAD) {
                        PistonHead pistonHead = (PistonHead) block.getBlockData();
                        mBlock.Material = MissileMaterial.PISTON;
                        mBlock.PistonData = new PistonData();
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
    @Override
    public boolean PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p) {
        Bounds box = PreProcessMissilePlacement(missile, location, world, isRed, p);
        if (box == null) return false;
        if(update){
            for(int i = box.getMinX(); i <= box.getMaxX(); i++){
                for(int j = box.getMinY(); j <= box.getMaxY(); j++){
                    for(int k = box.getMinZ(); k <= box.getMaxZ(); k++) {
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
        return true;
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
    public void PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p) {
        Vector location = origin.clone().add(block.Location);
        Block realBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
        }else if(block.Material == MissileMaterial.TNT){
            realBlock.setType(Material.TNT, false);
            CoreGame.Instance.mwMatch.Tracer.AddBlock(p.getUniqueId(), TraceType.TNT, location);
        }else if(block.Material == MissileMaterial.REDSTONE){
            realBlock.setType(Material.REDSTONE_BLOCK, false);
            CoreGame.Instance.mwMatch.Tracer.AddBlock(p.getUniqueId(), TraceType.REDSTONE, location);
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
    public ArrayList<MissileWarsItem> CreateDefaultItems() {
        ArrayList<MissileWarsItem> items = new ArrayList<>();
        items.add(CreateItem("Shieldbuster",
                1, 1, CreateItem(Material.WITCH_SPAWN_EGG, new String[]{
                        "&7Spawns a Shieldbuster Missile",
                        "&6Penetrates One Barrier",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &617"
                })));
        items.add(CreateItem("Guardian",
                1, 1, CreateItem(Material.GUARDIAN_SPAWN_EGG, new String[]{
                        "&7Spawns a Guardian Missile",
                        "&6Take it for a ride!",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &64"
                })));
        items.add(CreateItem("Lightning",
                1, 1, CreateItem(Material.OCELOT_SPAWN_EGG, new String[]{
                        "&7Spawns a Lightning Missile",
                        "&6&oOn your left!",
                        "&7Speed: &63.3 blocks/s",
                        "&7TNT: &612"
                })));
        items.add(CreateItem("Juggernaut",
                1, 1, CreateItem(Material.GHAST_SPAWN_EGG, new String[]{
                        "&7Spawns a Juggernaut Missile",
                        "&6Armed to the teeth",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &622"
                })));
        items.add(CreateItem("Tomahawk",
                1, 1, CreateItem(Material.CREEPER_SPAWN_EGG, new String[]{
                        "&7Spawns a Tomahawk Missile",
                        "&6The workhorse",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &615"
                })));
        items.add(CreateItem(MissileWarsCoreItem.FIREBALL.getValue(),
                1, 1, CreateItem(Material.BLAZE_SPAWN_EGG, new String[]{
                        "&7Spawns a punchable fireball",
                        "&6Use it to explode incoming missiles!"
                })));
        ItemStack gbis = new ItemStack(Material.BOW);
        ItemMeta mt = gbis.getItemMeta();
        mt.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
        mt.addEnchant(Enchantment.ARROW_FIRE, 1, true);
        mt.setUnbreakable(true);
        mt.setLore(Collections.singletonList("&6Use it to attack others!"));
        gbis.setItemMeta(mt);
        MissileWarsItem gbim = CreateItem(MissileWarsCoreItem.GUNBLADE.getValue(),
                1, 1, gbis);
        gbim.IsExempt = true;
        items.add(gbim);
        MissileWarsItem sim = CreateItem(MissileWarsCoreItem.SHIELD.getValue(),
                1, 1, CreateItem(Material.SNOWBALL, new String[]{
                                "&7Throw it in the air to deploy a barrier",
                                "&cIt is destroyed if it hits a block",
                                "&6Deploys after 1.0s"
                        }
                ));
        sim.IsShield = true;
        items.add(sim);
        items.add(CreateItem(MissileWarsCoreItem.ARROW.getValue(),
                3, 3, CreateItem(Material.ARROW, new String[0])));
        return items;
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
    public boolean SpawnShield(Vector location, World world, boolean isRed) {
        Map<Vector, Integer> shield = ShieldData(isRed);
        ArrayList<Vector> realLocation = new ArrayList<>();
        for(Vector key : shield.keySet()){
            realLocation.add(location.clone().add(key));
        }
        if(!CoreGame.Instance.mwMatch.CheckCanSpawn(isRed ?
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
            Material mat = block.getType();
            block.setType(mat, true);
        }
        return true;
    }

    public ItemStack CreateItem(Material type, String[] lore){
        ItemStack istack = new ItemStack(type);
        if(lore.length != 0){
            ItemMeta meta = istack.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            istack.setItemMeta(meta);
        }
        return istack;
    }
}
