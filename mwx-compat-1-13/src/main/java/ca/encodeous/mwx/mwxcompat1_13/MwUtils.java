package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class MwUtils {

    public static MissileWarsItem CreateItem(String id, int ss, int mss, ItemStack stack){
        MissileWarsItem i = new MissileWarsItem();
        i.MissileWarsItemId = id;
        i.MaxStackSize = mss;
        i.StackSize = ss;
        i.BaseItemStack = stack;
        i.IsExempt = false;
        i.IsShield = false;
        return i;
    }
    public static ItemStack CreateItem(MissileWarsItem item) {
        if(item.MissileWarsItemId.equals("Arrow")){
            return item.BaseItemStack.clone();
        }
        ItemStack itemstack = item.BaseItemStack.clone();
        ItemMeta meta = itemstack.getItemMeta();
        meta.setDisplayName(Chat.FCL("&6"+item.MissileWarsItemId+"&r"));
        ArrayList<String> lst = new ArrayList<>();
        if(meta.hasLore()) meta.getLore().stream().map(Chat::FCL).forEach(lst::add);
        lst.add(Chat.FCL("&0msw-internal:") + item.MissileWarsItemId);
        meta.setLore(lst);
        itemstack.setItemMeta(meta);
        return itemstack;
    }

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
