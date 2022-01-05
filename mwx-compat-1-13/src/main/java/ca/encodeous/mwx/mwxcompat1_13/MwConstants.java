package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ca.encodeous.mwx.mwxcompat1_13.MwUtils.CreateItem;

public class MwConstants {

    public static Map<Vector, Integer> ShieldData(boolean isRed){
        // 1 - pink glass, 2 - white glass, 3 - red glass, 4 - light gray glass, 5 - gray glass, 6 - black glass, 7 - black glass panes, 8 - lime glass, 9 - green glass
        Map<Vector, Integer> shield = new HashMap<>();
        if(isRed){
            shield.put(new Vector(), 1);
            shield.put(new Vector(1,0,0), 3);
            shield.put(new Vector(0,1,0), 3);
            shield.put(new Vector(-1,0,0), 3);
            shield.put(new Vector(0,-1,0), 3);
        }else{
            shield.put(new Vector(), 8);
            shield.put(new Vector(1,0,0), 9);
            shield.put(new Vector(0,1,0), 9);
            shield.put(new Vector(-1,0,0), 9);
            shield.put(new Vector(0,-1,0), 9);
        }
        // light gray
        shield.put(new Vector(-2, 0, 0), 4);
        shield.put(new Vector(-3, 0, 0), 4);
        shield.put(new Vector(2, 0, 0), 4);
        shield.put(new Vector(3, 0, 0), 4);
        shield.put(new Vector(0, -2, 0), 4);
        shield.put(new Vector(0, -3, 0), 4);
        shield.put(new Vector(0, 3, 0), 4);
        shield.put(new Vector(0, 2, 0), 4);
        // white
        shield.put(new Vector(1, 1, 0), 2);
        shield.put(new Vector(1, -1, 0), 2);
        shield.put(new Vector(-1, -1, 0), 2);
        shield.put(new Vector(-1, 1, 0), 2);
        // gray
        shield.put(new Vector(2, -1, 0), 5);
        shield.put(new Vector(2, 1, 0), 5);
        shield.put(new Vector(-2, -1, 0), 5);
        shield.put(new Vector(-2, 1, 0), 5);
        shield.put(new Vector(1, -2, 0), 5);
        shield.put(new Vector(-1, -2, 0), 5);
        shield.put(new Vector(1, 2, 0), 5);
        shield.put(new Vector(-1, 2, 0), 5);
        // black
        shield.put(new Vector(1, -3, 0), 6);
        shield.put(new Vector(-1, -3, 0), 6);
        shield.put(new Vector(-1, 3, 0), 6);
        shield.put(new Vector(1, 3, 0), 6);
        shield.put(new Vector(-3, 1, 0), 6);
        shield.put(new Vector(-3, -1, 0), 6);
        shield.put(new Vector(3, 1, 0), 6);
        shield.put(new Vector(3, -1, 0), 6);
        shield.put(new Vector(2, 2, 0), 6);
        shield.put(new Vector(2, -2, 0), 6);
        shield.put(new Vector(-2, 2, 0), 6);
        shield.put(new Vector(-2, -2, 0), 6);
        // panes
        shield.put(new Vector(3, 2, 0), 7);
        shield.put(new Vector(3, -2, 0), 7);
        shield.put(new Vector(-3, -2, 0), 7);
        shield.put(new Vector(-3, 2, 0), 7);
        shield.put(new Vector(2, 3, 0), 7);
        shield.put(new Vector(-2, 3, 0), 7);
        shield.put(new Vector(-2, -3, 0), 7);
        shield.put(new Vector(2, -3, 0), 7);
        return shield;
    }

    public static ArrayList<MissileWarsItem> CreateDefaultItems() {
        ArrayList<MissileWarsItem> items = new ArrayList<>();
        items.add(CreateItem("Shieldbuster",
                1, 1, CreateItem(Material.WITCH_SPAWN_EGG, new String[]{
                        "&7Spawns a Shieldbuster Missile",
                        "&6Penetrates One Barrier",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &617"
                })));
        items.add(CreateItem("Guardian",
                1, 1, CreateItem(Material.GUARDIAN_SPAWN_EGG, new String[]{
                        "&7Spawns a Guardian Missile",
                        "&6Take it for a ride!",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &64"
                })));
        items.add(CreateItem("Lightning",
                1, 1, CreateItem(Material.OCELOT_SPAWN_EGG, new String[]{
                        "&7Spawns a Lightning Missile",
                        "&6&oOn your left!",
                        "&7Speed: &63.3 blocks/s",
                        "&7TNT: &612"
                })));
        items.add(CreateItem("Juggernaut",
                1, 1, CreateItem(Material.GHAST_SPAWN_EGG, new String[]{
                        "&7Spawns a Juggernaut Missile",
                        "&6Armed to the teeth",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &622"
                })));
        items.add(CreateItem("Tomahawk",
                1, 1, CreateItem(Material.CREEPER_SPAWN_EGG, new String[]{
                        "&7Spawns a Tomahawk Missile",
                        "&6The workhorse",
                        "&7Speed: &61.7 blocks/s",
                        "&7TNT: &615"
                })));
        items.add(CreateItem(MissileWarsCoreItem.FIREBALL.getValue(),
                1, 1, CreateItem(Material.BLAZE_SPAWN_EGG, new String[]{
                        "&7Spawns a punchable fireball",
                        "&6Use it to explode incoming missiles!"
                })));
        ItemStack gbis = new ItemStack(Material.BOW);
        ItemMeta mt = gbis.getItemMeta();
        mt.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
        mt.addEnchant(Enchantment.ARROW_FIRE, 1, true);
        mt.setUnbreakable(true);
        mt.setLore(Collections.singletonList("&6Use it to attack others!"));
        gbis.setItemMeta(mt);
        MissileWarsItem gbim = CreateItem(MissileWarsCoreItem.GUNBLADE.getValue(),
                1, 1, gbis);
        gbim.IsExempt = true;
        items.add(gbim);
        MissileWarsItem sim = CreateItem(MissileWarsCoreItem.SHIELD.getValue(),
                1, 1, CreateItem(Material.SNOWBALL, new String[]{
                                "&7Throw it in the air to deploy a barrier",
                                "&cIt is destroyed if it hits a block",
                                "&6Deploys after 1.0s"
                        }
                ));
        sim.IsShield = true;
        items.add(sim);
        items.add(CreateItem(MissileWarsCoreItem.ARROW.getValue(),
                3, 3, CreateItem(Material.ARROW, new String[0])));
        return items;
    }
}
