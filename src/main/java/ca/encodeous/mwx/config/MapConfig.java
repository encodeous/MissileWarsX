package ca.encodeous.mwx.config;

import ca.encodeous.mwx.data.BoundingRegion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a per-map YAML configuration file loaded from maps/{name}.yml.
 */
public class MapConfig {

    public String name;
    public String worldName;
    public String schematic;

    // Spawn positions (lobby, per team)
    public Vector spawn;
    public float spawnYaw;
    public Vector greenSpawn;
    public float greenYaw;
    public Vector redSpawn;
    public float redYaw;
    public Vector greenLobby;
    public float greenLobbyYaw;
    public Vector redLobby;
    public float redLobbyYaw;


    // Portal regions
    public BoundingRegion greenPortal;
    public BoundingRegion redPortal;
    public BoundingRegion boundingBox;
    public BoundingRegion maxBoundingBox;
    public BoundingRegion barrier;

    // Join pads (list of "x,y,z" strings for auto-join, or split red/green)
    public Set<Vector> autoJoinPads = new HashSet<>();
    public Set<Vector> redJoinPads = new HashSet<>();
    public Set<Vector> greenJoinPads = new HashSet<>();

    // Special blocks
    public Set<Vector> returnToLobbyBlocks = new HashSet<>();
    public Set<Vector> spectateBlocks = new HashSet<>();

    /**
     * Load a MapConfig from a YAML file.
     */
    public static MapConfig fromFile(File f) {
        MapConfig cfg = new MapConfig();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);

        String fileName = f.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        String defaultName = extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;

        cfg.name = yaml.getString("name", defaultName);
        cfg.worldName = yaml.getString("world", "missilewars_game");
        cfg.schematic = yaml.getString("schematic");

        cfg.spawn = parseVec(yaml.getString("spawn", "0,64,0"));
        cfg.spawnYaw = (float) yaml.getDouble("spawn-yaw", 90.0);

        cfg.greenSpawn = parseVec(yaml.getString("green-spawn", "0,64,0"));
        cfg.greenYaw = (float) yaml.getDouble("green-yaw", 180.0);

        cfg.redSpawn = parseVec(yaml.getString("red-spawn", "0,64,0"));
        cfg.redYaw = (float) yaml.getDouble("red-yaw", 0.0);

        cfg.greenLobby = parseVec(yaml.getString("green-lobby", "0,64,0"));
        cfg.greenLobbyYaw = (float) yaml.getDouble("green-lobby-yaw", 90.0);

        cfg.redLobby = parseVec(yaml.getString("red-lobby", "0,64,0"));
        cfg.redLobbyYaw = (float) yaml.getDouble("red-lobby-yaw", 90.0);


        cfg.greenPortal = loadRegion(yaml, "green-portal");
        cfg.redPortal = loadRegion(yaml, "red-portal");
        cfg.boundingBox = loadRegion(yaml, "bounding-box");
        cfg.maxBoundingBox = loadRegion(yaml, "max-bounding-box");
        cfg.barrier = loadRegion(yaml, "barrier");

        // Join pads
        for (String s : yaml.getStringList("auto-join-pads")) {
            cfg.autoJoinPads.add(parseVec(s));
        }
        for (String s : yaml.getStringList("red-join-pads")) {
            cfg.redJoinPads.add(parseVec(s));
        }
        for (String s : yaml.getStringList("green-join-pads")) {
            cfg.greenJoinPads.add(parseVec(s));
        }
        for (String s : yaml.getStringList("return-to-lobby")) {
            cfg.returnToLobbyBlocks.add(parseVec(s));
        }
        for (String s : yaml.getStringList("spectate")) {
            cfg.spectateBlocks.add(parseVec(s));
        }

        return cfg;
    }

    public static Vector parseVec(String s) {
        if (s == null) return new Vector(0, 64, 0);
        String[] parts = s.trim().split(",");
        return new Vector(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
        );
    }

    private static BoundingRegion loadRegion(YamlConfiguration yaml, String path) {
        if (yaml.isList(path)) {
            return BoundingRegion.fromConfig(yaml.getStringList(path));
        }
        if (yaml.isConfigurationSection(path)) {
            String min = yaml.getString(path + ".min");
            String max = yaml.getString(path + ".max");
            if (min != null && max != null) {
                return BoundingRegion.fromConfig(min, max);
            }
        }
        return null;
    }
}
