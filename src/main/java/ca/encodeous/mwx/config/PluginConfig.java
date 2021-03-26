package ca.encodeous.mwx.config;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Reads and holds the top-level config.yml for the plugin.
 */
public class PluginConfig {

    public int maxTeamSize;
    public int resupplySeconds;
    public int drawCheckSeconds;
    public int startCountdownSeconds;
    public String mapName;
    public List<ItemConfig> items;
    public Map<String, Integer> breakSpeeds;
    public Map<SoundType, List<Sound>> eventSounds;
    public Map<ItemType, List<Sound>> deploySounds;

    private final Map<String, ItemConfig> itemById = new HashMap<>();

    public static PluginConfig load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        PluginConfig cfg = new PluginConfig();
        cfg.maxTeamSize = plugin.getConfig().getInt("max-team-size", 5);
        cfg.resupplySeconds = plugin.getConfig().getInt("resupply-seconds", 15);
        cfg.drawCheckSeconds = plugin.getConfig().getInt("draw-check-seconds", 5);
        cfg.startCountdownSeconds = plugin.getConfig().getInt("start-countdown-seconds", 30);
        cfg.mapName = plugin.getConfig().getString("map", "classic");

        cfg.breakSpeeds = new HashMap<>();
        ConfigurationSection bs = plugin.getConfig().getConfigurationSection("break-speeds");
        if (bs != null) {
            for (String key : bs.getKeys(false)) {
                cfg.breakSpeeds.put(key.toUpperCase(), bs.getInt(key));
            }
        }

        cfg.items = ItemConfig.deserialize(plugin.getConfig().getMapList("items"));
        for (ItemConfig item : cfg.items) {
            cfg.itemById.put(item.id, item);
        }

        var sounds = plugin.getConfig().getConfigurationSection("sounds");
        assert sounds != null;
        cfg.eventSounds = new HashMap<>();
        cfg.deploySounds = new HashMap<>();

        for (SoundType type : SoundType.values()) {
            cfg.eventSounds.put(type, getConfigSound(plugin, sounds, type.name()));
        }
        var deploySounds = sounds.getConfigurationSection("deploy");
        assert deploySounds != null;
        for (ItemType type : ItemType.values()) {
            if(deploySounds.contains(type.name().toLowerCase())) {
                cfg.deploySounds.put(type, getConfigSound(plugin, deploySounds, type.name().toLowerCase()));
            }
            else {
                cfg.deploySounds.put(type, getConfigSound(plugin, deploySounds, "default"));
            }
        }

        return cfg;
    }

    private static List<Sound> getConfigSound(JavaPlugin plugin, ConfigurationSection sounds, String name) {
        List<Sound> soundList = new ArrayList<>();
        if (sounds.contains(name.toLowerCase())) {
            for (String sound : sounds.getStringList(name.toLowerCase())) {
                var s = getSound(plugin, sound);
                if(s != null) soundList.add(s);
            }
        }
        return soundList;
    }

    public static Sound getSound(JavaPlugin plugin, String key) {
        var s = RegistryAccess
                .registryAccess()
                .getRegistry(RegistryKey.SOUND_EVENT)
                .get(new NamespacedKey("minecraft", key));
        if (s != null) {
            return s;
        } else {
            plugin.getLogger().warning("Unknown sound: " + key);
        }
        return null;
    }

    public ItemConfig getItem(String id) {
        return itemById.get(id);
    }

    public List<ItemConfig> getDistributableItems() {
        List<ItemConfig> result = new ArrayList<>();
        for (ItemConfig item : items) {
            if (!item.giveAtBeginning) result.add(item);
        }
        return result;
    }
}
