package ca.encodeous.mwx;

import ca.encodeous.mwx.config.MapConfig;
import ca.encodeous.mwx.config.PluginConfig;
import ca.encodeous.mwx.game.MwMap;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.structure.StructureRegistry;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.logging.Level;

/**
 * Singleton game controller — manages the one active match.
 */
public class MwGame {

    private static MwGame instance;

    private final MwPlugin plugin;
    private PluginConfig config;
    private StructureRegistry structures;
    private MwMatch activeMatch;
    private MwMap currentMap;

    private MwGame(MwPlugin plugin) {
        this.plugin = plugin;
    }

    public static void initialize(MwPlugin plugin) {
        instance = new MwGame(plugin);
        instance.startup();
    }

    public static MwGame getInstance() {
        return instance;
    }

    /**
     * Load config, map, and create match.
     */
    public void startup() {
        config = PluginConfig.load(plugin);
        structures = new StructureRegistry(plugin);
        structures.loadStructures();

        loadCurrentMap(config.mapName);
    }

    private void loadCurrentMap(String mapName) {
        File mapsDir = new File(plugin.getDataFolder(), "maps");
        mapsDir.mkdirs();
        File mapFile = new File(mapsDir, mapName + ".yml");
        if (!mapFile.exists()) {
            // Try to extract from plugin resources
            try {
                plugin.saveResource("maps/" + mapName + ".yml", false);
            } catch (Exception ignored) {
                // Resource doesn't exist in JAR
            }
        }
        if (!mapFile.exists()) {
            plugin.getLogger().warning("Map config not found: maps/" + mapName + ".yml");
            return;
        }
        MapConfig mapCfg;
        try {
            mapCfg = MapConfig.fromFile(mapFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load map config: " + mapName, e);
            return;
        }

        currentMap = MwMap.fromConfig(mapCfg);
        currentMap.initWorlds();
        createNewMatch();
    }

    public void shutdown() {
        if (activeMatch != null) {
            activeMatch.endGame();
            activeMatch = null;
        }
    }

    public MwMatch getMatch() {
        return activeMatch;
    }

    public PluginConfig getConfig() {
        return config;
    }

    public StructureRegistry getStructureRegistry() {
        return structures;
    }

    public MwMap getCurrentMap() {
        return currentMap;
    }

    /**
     * Create a new MwMatch for the current map. Called after map is ready.
     */
    public void createNewMatch() {
        if (currentMap == null) return;
        activeMatch = new MwMatch(currentMap);
        currentMap.loadMap(() -> {
            activeMatch.onMapReady();
            plugin.getLogger().info("Map loaded and match initialized.");
        });
    }

    /**
     * Reload plugin configuration (map is not reloaded).
     */
    public void reloadConfig() {
        config = PluginConfig.load(plugin);
        structures.loadStructures();
        plugin.getLogger().info("Config reloaded.");
    }

    /**
     * Change the map for the next game.
     */
    public void setMap(String mapName) {
        if (activeMatch != null) {
            activeMatch.endGame();
            activeMatch = null;
        }
        loadCurrentMap(mapName);
    }

    // ======================== Static helpers for event handlers ========================

    /**
     * Get the active match if the given world is the game world.
     */
    public static MwMatch fromWorld(World world) {
        if (instance == null || instance.activeMatch == null) return null;
        MwMap map = instance.activeMatch.getMap();
        if (map.gameWorld != null && map.gameWorld.equals(world)) {
            return instance.activeMatch;
        }
        return null;
    }

    /**
     * Get the active match if the player is participating in it.
     */
    public static MwMatch fromPlayer(Player p) {
        if (instance == null || instance.activeMatch == null) return null;
        return instance.activeMatch;
    }
}
