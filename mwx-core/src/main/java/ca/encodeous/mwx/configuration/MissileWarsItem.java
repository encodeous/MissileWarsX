package ca.encodeous.mwx.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MissileWarsItem implements ConfigurationSerializable {
    /**
     * Unique ID assigned to missile wars items
     */
    public String MissileWarsItemId;
    /**
     * Base Item Info
     */
    public ItemStack BaseItemStack;
    /**
     * Display name for the item when given to the Red Team
     */
    public String RedItemName;
    /**
     * Display name for the item when given to the Green Team
     */
    public String GreenItemName;
    /**
     * Number of this item that is given to the player each time
     */
    public int StackSize;
    /**
     * The max number of this item a player is allowed to have
     */
    public int MaxStackSize;
    /**
     * Is the item a shield?
     */
    public boolean IsShield;
    /**
     * Should this item not be given out?
     */
    public boolean IsExempt;
    public static MissileWarsItem deserialize(Map<String, Object> args) {
        MissileWarsItem item = new MissileWarsItem();
        item.MissileWarsItemId = (String)args.get("item-id");
        item.RedItemName = (String)args.get("red-name");
        item.GreenItemName = (String)args.get("green-name");
        item.StackSize = (Integer)args.get("stack-size");
        item.MaxStackSize = (Integer)args.get("max-stack-size");
        item.IsShield = (Boolean)args.get("is-shield");
        item.IsExempt = (Boolean)args.get("is-exempt");
        item.BaseItemStack = (ItemStack) args.get("base-item");
        return item;
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> o = new HashMap<>();
        o.put("item-id", this.MissileWarsItemId);
        o.put("red-name", this.RedItemName);
        o.put("green-name", this.GreenItemName);
        o.put("base-item", this.BaseItemStack);
        o.put("stack-size", this.StackSize);
        o.put("max-stack-size", this.MaxStackSize);
        o.put("is-shield", this.IsShield);
        o.put("is-exempt", this.IsExempt);
        return o;
    }
}
