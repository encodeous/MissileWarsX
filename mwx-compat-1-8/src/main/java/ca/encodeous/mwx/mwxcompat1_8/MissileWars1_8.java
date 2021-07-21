package ca.encodeous.mwx.mwxcompat1_8;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.MissileWarsImplementation;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMap;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceType;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import ca.encodeous.mwx.mwxcore.utils.WorldCopy;
import ca.encodeous.mwx.mwxcore.world.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MissileWars1_8 implements MissileWarsImplementation {
    // https://github.com/Bimmr/BimmCore
    public static class TitleAPI {

        private static Class<?> chatSerializer;
        private static Method serializer;
        private static Class<?> chatBaseComponent;
        private static Constructor<?> chatConstructor;
        private static Constructor<?> timeConstructor;
        private static Class<?> titleAction;
        private static Object timeEnum, titleEnum, subEnum, resetEnum;

        static {
            chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
            chatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
            titleAction = Reflection.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

            try {
                serializer = chatSerializer.getMethod("a", String.class);

                Class<?> packetType = Reflection.getNMSClass("PacketPlayOutTitle");
                chatConstructor = packetType.getConstructor(titleAction, chatBaseComponent);
                timeConstructor = packetType.getConstructor(titleAction, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            titleEnum = titleAction.getEnumConstants()[0];
            subEnum = titleAction.getEnumConstants()[1];

            if (Reflection.getVersion().startsWith("v1_7_") || Reflection.getVersion().startsWith("v1_8_") || Reflection.getVersion().startsWith("v1_9_") || Reflection.getVersion().startsWith("v1_10_"))
                timeEnum = titleAction.getEnumConstants()[2];
            else {
                timeEnum = titleAction.getEnumConstants()[3];
                resetEnum = titleAction.getEnumConstants()[5];
            }

        }

        /**
         * Send title.
         *
         * @param player   the player
         * @param title    the title
         * @param subTitle the sub title
         * @param fadeIn   the fade in
         * @param show     the show
         * @param fadeOut  the fade out
         */
        public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int show, int fadeOut) {
            try {
                Object lengthPacket = timeConstructor.newInstance(timeEnum, null, fadeIn, show, fadeOut);
                sendPacket(player, lengthPacket);

                Object titleSerialized = serializer.invoke(null, "{\"text\":\"" + title + "\"}");
                Object titlePacket = chatConstructor.newInstance(titleEnum, titleSerialized);
                sendPacket(player, titlePacket);

                if (subTitle != "") {
                    Object subSerialized = serializer.invoke(null, "{\"text\":\"" + subTitle + "\"}");
                    Object subPacket = chatConstructor.newInstance(subEnum, subSerialized);
                    sendPacket(player, subPacket);
                }

            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }

        /**
         * Reset.
         *
         * @param player the player
         */
        public static void reset(Player player) {
            try {
                if (resetEnum == null)
                    sendTitle(player, "", "", 0, 0, 0);
                else {
                    Object titlePacket = chatConstructor.newInstance(resetEnum, null);
                    sendPacket(player, titlePacket);
                }
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        private static Class<?> classPacket = Reflection.getNMSClass("Packet");
        public static void sendPacket(Player player, Object packet) {
            Object handle = Reflection.getHandle(player);
            Object playerConnection = Reflection.get(handle.getClass(), "playerConnection", handle);
            Method methodSend = Reflection.getMethod(playerConnection.getClass(), "sendPacket", classPacket);
            Reflection.invokeMethod(methodSend, playerConnection, new Object[]{packet});
        }
    }
    static class ActionBarAPIOld {
        private static Class<?> classPacket = Reflection.getNMSClass("Packet");
        private static Class<?>       chatSerializer;
        private static Class<?>       chatBaseComponent;
        private static Method serializer;
        private static Constructor<?> chatConstructor;
        static {
            chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
            chatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");

            try {
                serializer = chatSerializer.getMethod("a", String.class);

                Class<?> packetType = Reflection.getNMSClass("PacketPlayOutChat");
                chatConstructor = packetType.getConstructor(chatBaseComponent, byte.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        public static void sendPacket(Player player, Object packet) {
            Object handle = Reflection.getHandle(player);
            Object playerConnection = Reflection.get(handle.getClass(), "playerConnection", handle);
            Method methodSend = Reflection.getMethod(playerConnection.getClass(), "sendPacket", classPacket);
            Reflection.invokeMethod(methodSend, playerConnection, new Object[]{packet});
        }
        /**
         * Send the title
         *
         * @param player
         * @param msg
         */
        private static void sendActionBar(Player player, String msg) {
            try {
                Object serialized = serializer.invoke(null, "{\"text\":\"" + msg + "\"}");

                Object packet = chatConstructor.newInstance(serialized, (byte) 2);
                sendPacket(player, packet);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }
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
        TitleAPI.sendTitle(p, Formatter.FCL(title), Formatter.FCL(subtitle), 10, 20 * 5, 10);
    }

    @Override
    public void SendActionBar(Player p, String message) {
        ActionBarAPIOld.sendActionBar(p, Formatter.FCL(message));
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
        p.getInventory().addItem(CreateItem(CoreGame.Instance.GetItemById(MissileWarsCoreItem.GUNBLADE.getValue()), isRedTeam));
    }

    @Override
    public void ConfigureScoreboards(MissileWarsMatch mtch) {
        mtch.mwScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        mtch.mwGreen = mtch.mwScoreboard.registerNewTeam("green");
        mtch.mwGreen.setPrefix("§a");
        mtch.mwGreen.setNameTagVisibility(NameTagVisibility.ALWAYS);

        mtch.mwRed = mtch.mwScoreboard.registerNewTeam("red");
        mtch.mwRed.setPrefix("§c");
        mtch.mwRed.setNameTagVisibility(NameTagVisibility.ALWAYS);

        mtch.mwSpectate = mtch.mwScoreboard.registerNewTeam("spectator");
        mtch.mwSpectate.setPrefix("§9");
        mtch.mwSpectate.setAllowFriendlyFire(false);
        mtch.mwSpectate.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        mtch.mwSpectate.setCanSeeFriendlyInvisibles(true);

        mtch.mwLobby = mtch.mwScoreboard.registerNewTeam("lobby");
        mtch.mwLobby.setPrefix("§7");
        mtch.mwLobby.setAllowFriendlyFire(false);
        mtch.mwLobby.setNameTagVisibility(NameTagVisibility.ALWAYS);
    }

    @Override
    public void RegisterEvents(MissileWarsEvents events, JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(new MissileWarsEventHandler(events), plugin);
    }

    @Override
    public void FastCloneWorld(String targetName, String source) {
        Bukkit.unloadWorld(targetName, false);
        File worldFile = new File(Bukkit.getWorldContainer() + "/" + targetName);
        File srcWorldFile = new File(Bukkit.getWorldContainer() + "/" + source);
        WorldCopy.copyWorld(srcWorldFile, worldFile);
        WorldCreator wc = new WorldCreator(targetName);
        wc.type(WorldType.FLAT);
        wc.environment(World.Environment.NORMAL);
        wc.generator(new VoidWorldGen());
        wc.seed(0);
        wc.createWorld();
        World world = Bukkit.createWorld(wc);
        world.setAutoSave(false);
        world.setTicksPerAnimalSpawns(1000000000);
        world.setTicksPerMonsterSpawns(1000000000);
        world.setWaterAnimalSpawnLimit(0);
        world.setAnimalSpawnLimit(0);
        world.setDifficulty(Difficulty.EASY);
    }

    @Override
    public MissileWarsMap CreateManualJoinMap(String name) {
        MissileWarsMap map = new MissileWarsMap();
        FastCloneWorld(name, "mwx_template_manual");
        map.SeparateJoin = true;
        map.RedJoin = new HashSet<>(Arrays.asList(new Vector(-118,65,-6), new Vector(-118,65,-7), new Vector(-118,65,-8), new Vector(-118,65,-9)));
        map.GreenJoin = new HashSet<>(Arrays.asList(new Vector(-118,65,9), new Vector(-118,65,9), new Vector(-118,65,7), new Vector(-118,65,6)));
        return getMissileWarsMap(name, map);
    }

    @Override
    public MissileWarsMap CreateAutoJoinMap(String name) {
        MissileWarsMap map = new MissileWarsMap();
        FastCloneWorld(name, "mwx_template_auto");
        map.SeparateJoin = false;
        map.AutoJoin = new HashSet<>(Arrays.asList(new Vector(-115,66,2), new Vector(-115,66,1), new Vector(-115,66,0), new Vector(-115,66,-1), new Vector(-115,66,-2)));
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
        map.SpawnYaw = 90;
        map.GreenYaw = -180;
        map.RedYaw = 0;
        return map;
    }

    @Override
    public ItemStack CreateItem(MissileWarsItem item, boolean isRedTeam) {
        if(item.MissileWarsItemId.equals("Arrow")){
            return item.BaseItemStack.clone();
        }
        ItemStack itemstack = item.BaseItemStack.clone();
        ItemMeta meta = itemstack.getItemMeta();
        meta.setDisplayName(Formatter.FCL("&6"+item.MissileWarsItemId+"&r"));
        ArrayList<String> lst = new ArrayList<>();
        if(meta.hasLore()) meta.getLore().stream().map(Formatter::FCL).forEach(lst::add);
        lst.add(Formatter.FCL("&0msw-internal:") + item.MissileWarsItemId);
        meta.setLore(lst);
        itemstack.setItemMeta(meta);
        return itemstack;
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
                if(lore.startsWith(Formatter.FCL("&0msw-internal:"))){
                    return lore.substring(15);
                }
            }
        }
        return "";
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
                    if (block.getType() == Material.PISTON_BASE) {
                        mBlock.Material = MissileMaterial.PISTON;
                        PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = false;
                        mBlock.PistonData.IsPowered = pbm.isPowered();
                        mBlock.PistonData.Face = pbm.getFacing();
                    } else if (block.getType() == Material.PISTON_STICKY_BASE) {
                        mBlock.Material = MissileMaterial.PISTON;
                        PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = false;
                        mBlock.PistonData.IsSticky = true;
                        mBlock.PistonData.IsPowered = pbm.isPowered();
                        mBlock.PistonData.Face = pbm.getFacing();
                    } else if (block.getType() == Material.PISTON_EXTENSION) {
                        PistonExtensionMaterial pem = new PistonExtensionMaterial(Material.PISTON_BASE, block.getData());
                        mBlock.Material = MissileMaterial.PISTON;
                        mBlock.PistonData = new PistonData();
                        mBlock.PistonData.IsHead = true;
                        mBlock.PistonData.IsSticky = pem.isSticky();
                        mBlock.PistonData.Face = pem.getAttachedFace();
                    } else if (block.getType() == Material.SLIME_BLOCK) {
                        mBlock.Material = MissileMaterial.SLIME;
                    } else if (block.getType() == Material.STAINED_GLASS) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.GLASS) {
                        mBlock.Material = MissileMaterial.GLASS;
                    } else if (block.getType() == Material.TNT) {
                        mBlock.Material = MissileMaterial.TNT;
                    } else if (block.getType() == Material.REDSTONE_BLOCK) {
                        mBlock.Material = MissileMaterial.REDSTONE;
                    } else if (block.getType() == Material.STAINED_CLAY) {
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
    public ArrayList<Vector> PlaceMissile(Missile missile, Vector location, World world, boolean isRed, boolean update, Player p) {
        ArrayList<Vector> placedBlocks = new ArrayList<>();
        List<MissileBlock> blocks;
        if(isRed){
            blocks = missile.Schematic.Blocks;
        }else{
            blocks = missile.Schematic.CreateOppositeSchematic().Blocks;
        }
        Bounds box = new Bounds();
        for(MissileBlock block : blocks){
            PlaceBlock(block, location, world, isRed, p);
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
                            byte data = block.getData();
                            block.setType(Material.STAINED_GLASS);
                            block.setType(originalType);
                            block.setData(data, true);
                        }
                    }
                }
            }
        }
        return placedBlocks;
    }

    @Override
    public void PlaceBlock(MissileBlock block, Vector origin, World world, boolean isRed, Player p) {
        Vector location = origin.clone().add(block.Location);
        Block realBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if(block.Material == MissileMaterial.PISTON){
            if(block.PistonData.IsHead){
                PistonExtensionMaterial pem = new PistonExtensionMaterial(Material.PISTON_EXTENSION);
                pem.setFacingDirection(block.PistonData.Face);
                pem.setSticky(block.PistonData.IsSticky);
                realBlock.setType(Material.PISTON_EXTENSION, false);
                realBlock.setData(pem.getData(), false);
            }else{
                if(block.PistonData.IsSticky){
                    PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_STICKY_BASE);
                    pbm.setFacingDirection(block.PistonData.Face);
                    pbm.setPowered(block.PistonData.IsPowered);
                    realBlock.setType(Material.PISTON_STICKY_BASE, false);
                    realBlock.setData(pbm.getData(), false);
                }else{
                    PistonBaseMaterial pbm = new PistonBaseMaterial(Material.PISTON_BASE);
                    pbm.setFacingDirection(block.PistonData.Face);
                    pbm.setPowered(block.PistonData.IsPowered);
                    realBlock.setType(Material.PISTON_BASE, false);
                    realBlock.setData(pbm.getData(), false);
                }
            }
        }else if(block.Material == MissileMaterial.SLIME){
            realBlock.setType(Material.SLIME_BLOCK, false);
        }else if(block.Material == MissileMaterial.GLASS){
            if(isRed){
                realBlock.setType(Material.STAINED_GLASS, false);
                realBlock.setData(DyeColor.RED.getData(), false);
            }else{
                realBlock.setType(Material.STAINED_GLASS, false);
                realBlock.setData(DyeColor.GREEN.getData(), false);
            }
        }else if(block.Material == MissileMaterial.CLAY){
            if(isRed){
                realBlock.setType(Material.STAINED_CLAY, false);
                realBlock.setData(DyeColor.RED.getData(), false);
            }else{
                realBlock.setType(Material.STAINED_CLAY, false);
                realBlock.setData(DyeColor.GREEN.getData(), false);
            }
        }else if(block.Material == MissileMaterial.TNT){
            realBlock.setType(Material.TNT, false);
            CoreGame.Instance.mwMatch.Tracer.AddBlock(p.getUniqueId(), TraceType.TNT, location);
        }else if(block.Material == MissileMaterial.REDSTONE){
            realBlock.setType(Material.REDSTONE_BLOCK, false);
            CoreGame.Instance.mwMatch.Tracer.AddBlock(p.getUniqueId(), TraceType.REDSTONE, location);
        }
    }

    public Map<Vector, Integer> ShieldData(boolean isRed){
        // 1 - pink glass, 2 - white glass, 3 - red glass, 4 - light gray glass, 5 - gray glass, 6 - black glass, 7 - black glass panes, 8 - lime glass, 9 - green glass
        Map<Vector, Integer> shield = new HashMap<>();
        if(isRed){
            shield.put(new Vector(), 1);
            shield.put(new Vector(1,0,0), 3);
            shield.put(new Vector(0,1,0), 3);
            shield.put(new Vector(-1,0,0), 3);
            shield.put(new Vector(0,-1,0), 3);
        }else{
            shield.put(new Vector(), 8);
            shield.put(new Vector(1,0,0), 9);
            shield.put(new Vector(0,1,0), 9);
            shield.put(new Vector(-1,0,0), 9);
            shield.put(new Vector(0,-1,0), 9);
        }
        // light gray
        shield.put(new Vector(-2, 0, 0), 4);
        shield.put(new Vector(-3, 0, 0), 4);
        shield.put(new Vector(2, 0, 0), 4);
        shield.put(new Vector(3, 0, 0), 4);
        shield.put(new Vector(0, -2, 0), 4);
        shield.put(new Vector(0, -3, 0), 4);
        shield.put(new Vector(0, 3, 0), 4);
        shield.put(new Vector(0, 2, 0), 4);
        // white
        shield.put(new Vector(1, 1, 0), 2);
        shield.put(new Vector(1, -1, 0), 2);
        shield.put(new Vector(-1, -1, 0), 2);
        shield.put(new Vector(-1, 1, 0), 2);
        // gray
        shield.put(new Vector(2, -1, 0), 5);
        shield.put(new Vector(2, 1, 0), 5);
        shield.put(new Vector(-2, -1, 0), 5);
        shield.put(new Vector(-2, 1, 0), 5);
        shield.put(new Vector(1, -2, 0), 5);
        shield.put(new Vector(-1, -2, 0), 5);
        shield.put(new Vector(1, 2, 0), 5);
        shield.put(new Vector(-1, 2, 0), 5);
        // black
        shield.put(new Vector(1, -3, 0), 6);
        shield.put(new Vector(-1, -3, 0), 6);
        shield.put(new Vector(-1, 3, 0), 6);
        shield.put(new Vector(1, 3, 0), 6);
        shield.put(new Vector(-3, 1, 0), 6);
        shield.put(new Vector(-3, -1, 0), 6);
        shield.put(new Vector(3, 1, 0), 6);
        shield.put(new Vector(3, -1, 0), 6);
        shield.put(new Vector(2, 2, 0), 6);
        shield.put(new Vector(2, -2, 0), 6);
        shield.put(new Vector(-2, 2, 0), 6);
        shield.put(new Vector(-2, -2, 0), 6);
        // panes
        shield.put(new Vector(3, 2, 0), 7);
        shield.put(new Vector(3, -2, 0), 7);
        shield.put(new Vector(-3, -2, 0), 7);
        shield.put(new Vector(-3, 2, 0), 7);
        shield.put(new Vector(2, 3, 0), 7);
        shield.put(new Vector(-2, 3, 0), 7);
        shield.put(new Vector(-2, -3, 0), 7);
        shield.put(new Vector(2, -3, 0), 7);
        return shield;
    }

    @Override
    public void SpawnShield(Vector location, World world, boolean isRed) {
        Map<Vector, Integer> shield = ShieldData(isRed);
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            if(e.getValue() == 1){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.PINK.getData(), false);
            }
            if(e.getValue() == 2){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.WHITE.getData(), false);
            }
            if(e.getValue() == 3){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.RED.getData(), false);
            }
            if(e.getValue() == 4){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.SILVER.getData(), false);
            }
            if(e.getValue() == 5){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.GRAY.getData(), false);
            }
            if(e.getValue() == 6){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.BLACK.getData(), false);
            }
            if(e.getValue() == 7){
                block.setType(Material.STAINED_GLASS_PANE, false);
                block.setData(DyeColor.BLACK.getData(), false);
            }
            if(e.getValue() == 8){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.LIME.getData(), false);
            }
            if(e.getValue() == 9){
                block.setType(Material.STAINED_GLASS, false);
                block.setData(DyeColor.GREEN.getData(), false);
            }
        }
        for(Map.Entry<Vector, Integer> e : shield.entrySet()){
            Block block = Utils.LocationFromVec(location.clone().add(e.getKey()), world).getBlock();
            byte data = block.getData();
            Material mat = block.getType();
            block.setType(mat, true);
            block.setData(data, true);
        }
    }

    @Override
    public void SummonFrozenFireball(Vector location, World world, Player p) {
        ArmorStand a = (ArmorStand) world.spawnEntity(Utils.LocationFromVec(location, world).add(0,-1.5,0), EntityType.ARMOR_STAND);
        a.setVisible(false);
        a.setGravity(false);
        Fireball e = (Fireball)world.spawnEntity(Utils.LocationFromVec(location, world), EntityType.FIREBALL);
        e.setYield(1.5f);
        a.setPassenger(e);
        e.setIsIncendiary(true);
    }

    @Override
    public void SetTntSource(TNTPrimed tnt, Player p) {
        // Doesnt do anything in 1.8
    }

    @Override
    public ArrayList<MissileWarsItem> CreateDefaultItems() {
        ArrayList<MissileWarsItem> items = new ArrayList<>();
        items.add(CreateItem("Shieldbuster",
                1, 1, CreateSpawnEgg(EntityType.WITCH, new String[]{
                        "&7Spawns a Shieldbuster Missile",
                        "&6Penetrates One Barrier",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &617"
                })));
        items.add(CreateItem("Guardian",
                1, 1, CreateSpawnEgg(EntityType.GUARDIAN, new String[]{
                        "&7Spawns a Guardian Missile",
                        "&6Take it for a ride!",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &64"
                })));
        items.add(CreateItem("Lightning",
                1, 1, CreateSpawnEgg(EntityType.OCELOT, new String[]{
                        "&7Spawns a Lightning Missile",
                        "&6&oOn your left!",
                        "&7Speed: &63.3 blocks/s",
                        "&7TNT: &612"
                })));
        items.add(CreateItem("Juggernaut",
                1, 1, CreateSpawnEgg(EntityType.GHAST, new String[]{
                        "&7Spawns a Juggernaut Missile",
                        "&6Armed to the teeth",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &622"
                })));
        items.add(CreateItem("Tomahawk",
                1, 1, CreateSpawnEgg(EntityType.CREEPER, new String[]{
                        "&7Spawns a Tomahawk Missile",
                        "&6The workhorse",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &615"
                })));
        items.add(CreateItem(MissileWarsCoreItem.FIREBALL.getValue(),
                1, 1, CreateSpawnEgg(EntityType.BLAZE, new String[]{
                        "&7Spawns a punchable fireball",
                        "&6Use it to explode incoming missiles!"
                })));
        ItemStack gbis = new ItemStack(Material.BOW);
        ItemMeta mt = gbis.getItemMeta();
        mt.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
        mt.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        mt.addEnchant(Enchantment.ARROW_FIRE, 1, true);
        mt.setLore(Collections.singletonList("&6Use it to attack others!"));
        mt.spigot().setUnbreakable(true);
        gbis.setItemMeta(mt);
        MissileWarsItem gbim = CreateItem(MissileWarsCoreItem.GUNBLADE.getValue(),
                1, 1, gbis);
        gbim.IsExempt = true;
        items.add(gbim);
        MissileWarsItem sim = CreateItem(MissileWarsCoreItem.SHIELD.getValue(),
                1, 1, CreateOtherItem(Material.SNOW_BALL, new String[]{
                                "&7Throw it in the air to deploy a barrier",
                                "&cIt is destroyed if it hits a block",
                                "&6Deploys after 1.0s"
                        }
                ));
        sim.IsShield = true;
        items.add(sim);
        items.add(CreateItem(MissileWarsCoreItem.ARROW.getValue(),
                3, 3, CreateOtherItem(Material.ARROW, new String[0])));
        return items;
    }

    public MissileWarsItem CreateItem(String id, int ss, int mss, ItemStack stack){
        MissileWarsItem i = new MissileWarsItem();
        i.MissileWarsItemId = id;
        i.MaxStackSize = mss;
        i.StackSize = ss;
        i.BaseItemStack = stack;
        i.IsExempt = false;
        i.IsShield = false;
        return i;
    }
    public ItemStack CreateSpawnEgg(EntityType type, String[] lore){
        ItemStack istack = new ItemStack(Material.MONSTER_EGG, 1, type.getTypeId());
        ItemMeta meta = istack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        istack.setItemMeta(meta);
        return istack;
    }
    public ItemStack CreateOtherItem(Material type, String[] lore){
        ItemStack istack = new ItemStack(type);
        if(lore.length != 0){
            ItemMeta meta = istack.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            istack.setItemMeta(meta);
        }
        return istack;
    }
}
