package ca.encodeous.mwx.structure;

import ca.encodeous.mwx.MwPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages all loaded structure (missile/shield) templates.
 * Schematics are read from the {@code structures/} folder inside the plugin data directory.
 */
public class StructureRegistry {

    private final Map<String, StructureTemplate> structures = new LinkedHashMap<>();
    private final MwPlugin plugin;

    public StructureRegistry(MwPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Scan the {@code structures/} folder and load each .schem file into memory via FAWE.
     */
    public void loadStructures() {
        structures.clear();
        File structuresDir = new File(plugin.getDataFolder(), "structures");
        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
            plugin.getLogger().info("Created structures/ directory. Place .schem files there.");
            return;
        }

        File[] files = structuresDir.listFiles((dir, name) ->
                name.endsWith(".schem") || name.endsWith(".schematic") || name.endsWith(".nbt"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No structure schematics found in structures/");
            return;
        }

        for (File f : files) {
            String id = f.getName().replaceFirst("\\.[^.]+$", ""); // strip extension
            try {
                StructureTemplate template = StructureTemplate.load(f, id);
                structures.put(id, template);
                plugin.getLogger().info("Loaded structure: " + id
                        + " [" + template.width + "x" + template.height + "x" + template.depth + "]");
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load structure: " + f.getName(), e);
            }
        }
        plugin.getLogger().info("Loaded " + structures.size() + " structure(s).");
    }

    public StructureTemplate get(String id) {
        return structures.get(id);
    }

    public Collection<StructureTemplate> getAll() {
        return Collections.unmodifiableCollection(structures.values());
    }
}
