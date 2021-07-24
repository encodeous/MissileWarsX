package ca.encodeous.mwx.mwxcompat1_13;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MwUtils extends ca.encodeous.mwx.mwxcompat1_8.MwUtils{
    public static ItemStack CreateItem(Material type, String[] lore){
        ItemStack istack = new ItemStack(type);
        if(lore.length != 0){
            ItemMeta meta = istack.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            istack.setItemMeta(meta);
        }
        return istack;
    }
}
