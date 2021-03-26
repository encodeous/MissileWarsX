package ca.encodeous.mwx.game;

import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.MapConfig;
import ca.encodeous.mwx.data.BoundingRegion;
import ca.encodeous.mwx.util.VoidWorldGen;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;

/**
 * Holds map configuration and handles schematic-based map reset operations.
 */
public class MwMap {

    /** All static map configuration (spawn points, pads, portal regions, etc.). */
    public MapConfig config;

    public World gameWorld;

    public volatile boolean busy = false;

    /**
     * Create an MwMap from a parsed MapConfig.
     */
    public static MwMap fromConfig(MapConfig cfg) {
        MwMap map = new MwMap();
        map.config = cfg;
        return map;
    }

    /**
     * Create or load the game world, configuring gamerules.
     */
    public void initWorlds() {
        gameWorld = loadOrCreateWorld(config.worldName, true);
        if (gameWorld != null) configureWorld(gameWorld);
    }

    private World loadOrCreateWorld(String name, boolean voidGen) {
        World w = Bukkit.getWorld(name);
        if (w != null) return w;
        WorldCreator creator = new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .type(WorldType.FLAT);
        if (voidGen) {
            creator.generator(new VoidWorldGen());
        }
        w = Bukkit.createWorld(creator);
        if (w == null) {
            MwPlugin.getInstance().getLogger().warning("Failed to create world: " + name);
        }
        return w;
    }

    /**
     * Paste the configured map schematic into the game world asynchronously using FAWE.
     * Calls callback on the main thread when done.
     */
    public void loadMap(Runnable callback) {
        resetMap(callback);
    }

    /**
     * Reset the map by pasting the configured schematic into the game world.
     */
    public void resetMap(Runnable callback) {
        if (gameWorld == null) {
            MwPlugin.getInstance().getLogger().warning("Cannot reset map: game world not loaded.");
            busy = false;
            if (callback != null) Bukkit.getScheduler().runTask(MwPlugin.getInstance(), callback);
            return;
        }
        if (config.schematic == null || config.schematic.isBlank()) {
            MwPlugin.getInstance().getLogger().warning("Cannot reset map: no schematic configured.");
            busy = false;
            if (callback != null) Bukkit.getScheduler().runTask(MwPlugin.getInstance(), callback);
            return;
        }

        clearEntities();

        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(MwPlugin.getInstance(), () -> {
            try {
                com.sk89q.worldedit.world.World weGame = BukkitAdapter.adapt(gameWorld);

                try (com.sk89q.worldedit.EditSession dstSession = WorldEdit.getInstance()
                        .newEditSessionBuilder().world(weGame).fastMode(true).build()) {
                    File schematicFile = resolveMapSchematic(config.schematic);
                    if (schematicFile == null) {
                        throw new IllegalStateException("Map schematic not found: " + config.schematic);
                    }

                    dstSession.setBlocks((Region) config.maxBoundingBox.toWorldEditRegion(gameWorld), BukkitAdapter.adapt(Material.AIR.createBlockData()));

                    Clipboard clipboard = loadClipboard(schematicFile);
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(dstSession)
                            .to(clipboard.getOrigin())
                            .ignoreAirBlocks(true)
                            .copyEntities(false)
                            .copyBiomes(false)
                            .build();

                    dstSession.setBlocks((Region) config.barrier.toWorldEditRegion(gameWorld), BukkitAdapter.adapt(Material.BARRIER.createBlockData()));
                    Operations.complete(operation);
                }

            } catch (Exception e) {
                MwPlugin.getInstance().getLogger().log(Level.SEVERE, "Error resetting map", e);
            } finally {
                busy = false;
                Bukkit.getScheduler().runTask(MwPlugin.getInstance(), this::clearEntities);
                if (callback != null) {
                    Bukkit.getScheduler().runTask(MwPlugin.getInstance(), callback);
                }
            }
        });
    }

    private File resolveMapSchematic(String schematicName) {
        File mapsDir = new File(MwPlugin.getInstance().getDataFolder(), "maps");
        File direct = new File(mapsDir, schematicName);
        if (direct.isFile()) {
            return direct;
        }

        for (String extension : new String[]{".schem"}) {
            File candidate = new File(mapsDir, schematicName + extension);
            if (candidate.isFile()) {
                return candidate;
            }
        }

        return null;
    }

    private Clipboard loadClipboard(File schematicFile) throws Exception {
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            throw new IllegalArgumentException("Unknown schematic format: " + schematicFile.getName());
        }

        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            return reader.read();
        }
    }

    /**
     * Remove all non-player entities from the game world.
     */
    public void clearEntities() {
        if (gameWorld == null) return;
        gameWorld.getEntities().forEach(entity -> {
            if (!(entity instanceof org.bukkit.entity.Player)) {
                entity.remove();
            }
        });
    }

    /**
     * Configure gamerules for a world.
     */
    private void configureWorld(World world) {
        world.setGameRule(GameRules.KEEP_INVENTORY, true);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.BLOCK_DROPS, false);
        world.setGameRule(GameRules.ENTITY_DROPS, false);
        world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        world.setGameRule(GameRules.RAIDS, false);
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false);
        world.setTime(6000);
        world.setAutoSave(false);
        world.setVoidDamageMinBuildHeightOffset(64);
        var bb = config.maxBoundingBox;
        world.getWorldBorder().setSize((bb.maxX - bb.minX));
        world.getWorldBorder().setCenter(bb.minX + (double) (bb.maxX - bb.minX) / 2, bb.minZ + (double) (bb.maxZ - bb.minZ) / 2);
    }

    public Location getLobbySpawn() {
        return new Location(gameWorld,
                config.spawn.getX(), config.spawn.getY(), config.spawn.getZ(),
                config.spawnYaw, 0);
    }

    public Location getRedSpawn() {
        return new Location(gameWorld,
                config.redSpawn.getX(), config.redSpawn.getY(), config.redSpawn.getZ(),
                config.redYaw, 0);
    }

    public Location getGreenSpawn() {
        return new Location(gameWorld,
                config.greenSpawn.getX(), config.greenSpawn.getY(), config.greenSpawn.getZ(),
                config.greenYaw, 0);
    }

    public Location getRedLobbyLocation() {
        return new Location(gameWorld,
                config.redLobby.getX(), config.redLobby.getY(), config.redLobby.getZ(),
                config.redLobbyYaw, 0);
    }

    public Location getGreenLobbyLocation() {
        return new Location(gameWorld,
                config.greenLobby.getX(), config.greenLobby.getY(), config.greenLobby.getZ(),
                config.greenLobbyYaw, 0);
    }
}
