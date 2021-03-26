package ca.encodeous.mwx.config;

import ca.encodeous.mwx.MwPlugin;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Represents one item/structure configuration entry from config.yml.
 */
public class ItemConfig {

    public String id;
    public Material material;
    public String displayName;
    /** ID of the structure schematic to deploy (optional, used for MISSILE and SHIELD item types). */
    public String schematicId;
    /** The special function this item performs when used. */
    public ItemType itemType;
    public int stackSize;
    public int maxStack;
    public List<String> lore = new ArrayList<>();
    public Map<Attribute, AttributeModifier> attributes = new HashMap<>();
    public Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
    public boolean unbreakable;
    /** If true, this item is given to every player at the start of the game. */
    public boolean giveAtBeginning;

    /**
     * Create an ItemStack with a PersistentDataContainer tag for item identification.
     */
    public ItemStack createItemStack(JavaPlugin plugin) {
        ItemStack stack = new ItemStack(material, stackSize);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if (!lore.isEmpty()) {
                List<String> translatedLore = new ArrayList<>(lore.size());
                for (String line : lore) {
                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(translatedLore);
            }
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            meta.setUnbreakable(unbreakable);
            for(var entry : attributes.entrySet()) {
                meta.addAttributeModifier(entry.getKey(), entry.getValue());
            }

            NamespacedKey key = new NamespacedKey(plugin, "item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Check if an ItemStack carries this item's ID tag.
     */
    public static String getItemId(JavaPlugin plugin, ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(plugin, "item_id");
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private static Enchantment parseEnchantment(String name) {
        return RegistryAccess
                .registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(new NamespacedKey("minecraft", name));
    }

    public static List<ItemConfig> deserialize(List<Map<?, ?>> rawItems) {
        var items = new ArrayList<ItemConfig>();
        var plugin = MwPlugin.getInstance();
        for (Map<?, ?> mv : rawItems) {
            var map = (Map<String, Object>) mv;
            ItemConfig item = new ItemConfig();
            item.id = (String) map.get("id");
            String matStr = (String) map.get("material");
            try {
                item.material = Material.valueOf(matStr.toUpperCase());
            } catch (Exception e) {
                plugin.getLogger().warning("Unknown material: " + matStr + " for item " + item.id);
                item.material = Material.STONE;
            }
            item.displayName = (String) map.getOrDefault("name", item.id);
            item.schematicId = (String) map.get("schematic");
            String typeStr = (String) map.getOrDefault("item-type", map.get("type"));
            if (typeStr != null) {
                try {
                    item.itemType = ItemType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Unknown item type: " + typeStr + " for item " + item.id);
                    item.itemType = null;
                }
            }
            item.stackSize = (int) map.getOrDefault("stack-size", 1);
            item.maxStack = (int) map.getOrDefault("max-stack", 1);
            Object loreValue = map.get("lore");
            if (loreValue instanceof List<?> loreList) {
                for (Object line : loreList) {
                    if (line != null) {
                        item.lore.add(String.valueOf(line));
                    }
                }
            }

            Object enchantsValue = map.get("enchants");
            if (enchantsValue instanceof Map<?, ?> enchantsMap) {
                for (Map.Entry<?, ?> entry : enchantsMap.entrySet()) {
                    if (entry.getKey() == null || !(entry.getValue() instanceof Number level)) {
                        continue;
                    }
                    Enchantment enchantment = parseEnchantment(String.valueOf(entry.getKey()));
                    if (enchantment == null) {
                        plugin.getLogger().warning("Unknown enchantment: " + entry.getKey() + " for item " + item.id);
                        continue;
                    }
                    item.enchantments.put(enchantment, level.intValue());
                }
            }
            Object attributesValue = map.get("attributes");
            if (attributesValue instanceof Map<?, ?> attributesMap) {
                item.attributes = new HashMap<>();
                for (Map.Entry<?, ?> entry : attributesMap.entrySet()) {
                    var key = Registry.ATTRIBUTE.getOrThrow(Key.key(Key.MINECRAFT_NAMESPACE, (String) entry.getKey()));
                    var val = (Map<String, Object>) entry.getValue();
                    val.put("key", key.getKey().value());
                    var attrValue = AttributeModifier.deserialize(val);
                    item.attributes.put(key, attrValue);
                }
            }

            item.unbreakable = (boolean) map.getOrDefault("unbreakable", false);
            item.giveAtBeginning = (boolean) map.getOrDefault("give-at-beginning", false);
            items.add(item);
        }
        return items;
    }
}
