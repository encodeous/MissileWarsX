package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcompat1_8.MissileWarsEventHandler;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
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
    }

    @Override
    public void ConfigureScoreboards(MissileWarsMatch mtch) {
        mtch.mwScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        mtch.mwGreen = mtch.mwScoreboard.registerNewTeam("green");
        mtch.mwGreen.setColor(ChatColor.GREEN);
        mtch.mwGreen.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        mtch.mwRed = mtch.mwScoreboard.registerNewTeam("red");
        mtch.mwRed.setColor(ChatColor.RED);
        mtch.mwRed.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        mtch.mwSpectate = mtch.mwScoreboard.registerNewTeam("spectator");
        mtch.mwSpectate.setColor(ChatColor.BLUE);
        mtch.mwSpectate.setAllowFriendlyFire(false);
        mtch.mwSpectate.setCanSeeFriendlyInvisibles(true);
        mtch.mwSpectate.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);

        mtch.mwLobby = mtch.mwScoreboard.registerNewTeam("lobby");
        mtch.mwLobby.setColor(ChatColor.GRAY);
        mtch.mwLobby.setAllowFriendlyFire(false);
        mtch.mwLobby.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
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
    public ArrayList<Vector> PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update) {
        ArrayList<Vector> placedBlocks = new ArrayList<>();
        List<MissileBlock> blocks;
        if(isRed){
            blocks = missile.Schematic.Blocks;
        }else{
            blocks = missile.Schematic.CreateOppositeSchematic().Blocks;
        }
        Bounds box = new Bounds();
        for(MissileBlock block : blocks){
            PlaceBlock(block, location, world, isRed);
            box.stretch(location.clone().add(block.Location));
            if(block.Material == MissileMaterial.TNT){
                placedBlocks.add(location.clone().add(block.Location));
            }
        }
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
        return placedBlocks;
    }

    public ItemStack MakeArmour(Material mat, Color color){
        ItemStack hstack = new ItemStack(mat);
        LeatherArmorMeta hdata = (LeatherArmorMeta) hstack.getItemMeta();
        hdata.setColor(color);
        hdata.addEnchant(Enchantment.DURABILITY, 32767, true);
        hdata.setUnbreakable(true);
        hdata.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        hstack.setItemMeta(hdata);
        return hstack;
    }

    @Override
    public void PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed) {
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
        }else if(block.Material == MissileMaterial.REDSTONE){
            realBlock.setType(Material.REDSTONE_BLOCK, false);
        }
    }

    @Override
    public ArrayList<MissileWarsItem> CreateDefaultItems() {
        ArrayList<MissileWarsItem> items = new ArrayList<>();
        items.add(CreateItem("shield_buster_spawn",
                "&c&lSpawn &f&lShield &6&lBuster",
                "&a&lSpawn &f&lShield &6&lBuster",
                1, 1, CreateItem(Material.WITCH_SPAWN_EGG, new String[]{
                        "&7Spawns a Shield Buster Missile",
                        "&6Penetrates One Barrier",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &617"
                })));
        items.add(CreateItem("guardian_spawn",
                "&c&lSpawn &6&lGuardian",
                "&a&lSpawn &6&lGuardian",
                1, 1, CreateItem(Material.GUARDIAN_SPAWN_EGG, new String[]{
                        "&7Spawns a Guardian Missile",
                        "&6Take it for a ride!",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &64"
                })));
        items.add(CreateItem("lightning_spawn",
                "&c&lSpawn &6&lLightning",
                "&a&lSpawn &6&lLightning",
                1, 1, CreateItem(Material.OCELOT_SPAWN_EGG, new String[]{
                        "&7Spawns a Lightning Missile",
                        "&6&oOn your left!",
                        "&7Speed: &63.3 blocks/s",
                        "&7TNT: &612"
                })));
        items.add(CreateItem("juggernaut_spawn",
                "&c&lSpawn &6&lJuggernaut",
                "&a&lSpawn &6&lJuggernaut",
                1, 1, CreateItem(Material.GHAST_SPAWN_EGG, new String[]{
                        "&7Spawns a Juggernaut Missile",
                        "&6Armed to the teeth",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &622"
                })));
        items.add(CreateItem("tomahawk_spawn",
                "&c&lSpawn &6&lTomahawk",
                "&a&lSpawn &6&lTomahawk",
                1, 1, CreateItem(Material.CREEPER_SPAWN_EGG, new String[]{
                        "&7Spawns a Tomahawk Missile",
                        "&6The workhorse",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &615"
                })));
        items.add(CreateItem("fireball_spawn",
                "&c&lSpawn &6&lFireball",
                "&a&lSpawn &6&lFireball",
                1, 1, CreateItem(Material.BLAZE_SPAWN_EGG, new String[]{
                        "&7Spawns a punchable fireball",
                        "&6Use it to explode incoming missiles!"
                })));
        ItemStack gbis = new ItemStack(Material.BOW);
        ItemMeta mt = gbis.getItemMeta();
        mt.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
        mt.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        mt.addEnchant(Enchantment.ARROW_FIRE, 1, true);
        mt.setUnbreakable(true);
        mt.setLore(Collections.singletonList("&6Use it to attack others!"));
        gbis.setItemMeta(mt);
        MissileWarsItem gbim = CreateItem("gunblade",
                "&c&lGun&7-&6&lBlade",
                "&a&lGun&7-&6&lBlade",
                1, 1, gbis);
        gbim.IsExempt = true;
        items.add(gbim);
        MissileWarsItem sim = CreateItem("shield_spawn",
                "&c&lDeploy &6&lShield",
                "&a&lDeploy &6&lShield",
                1, 1, CreateItem(Material.SNOWBALL, new String[]{
                                "&7Throw it in the air to deploy a barrier",
                                "&cIt is destroyed if it hits a block",
                                "&6Deploys after 1.0s"
                        }
                ));
        sim.IsShield = true;
        items.add(sim);
        items.add(CreateItem("arrow",
                "Arrow",
                "Arrow",
                3, 3, CreateItem(Material.ARROW, new String[0])));
        return items;
    }
    @Override
    public void SpawnShield(Vector location, World world, boolean isRed) {
        Map<Vector, Integer> shield = ShieldData(isRed);
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
