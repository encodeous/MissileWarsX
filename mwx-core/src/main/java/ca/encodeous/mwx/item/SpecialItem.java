package ca.encodeous.mwx.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public abstract class SpecialItem {

    public static final ArrayList<SpecialItem> ITEMS = new ArrayList<>();

    public static void initialize() {
        ITEMS.clear();
        ITEMS.add(new TntThrowerItem());
    }

    public abstract Material getItem();
    public abstract int getData();

    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(getItem());
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(getItem());
        meta.setCustomModelData(getData());
        stack.setItemMeta(meta);
        return stack;
    }

    public abstract void onUse(PlayerInteractEvent e);

}
