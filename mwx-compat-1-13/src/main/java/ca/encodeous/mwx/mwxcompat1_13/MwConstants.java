package ca.encodeous.mwx.mwxcompat1_13;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;

import static ca.encodeous.mwx.mwxcompat1_13.MwUtils.CreateItem;

public class MwConstants extends ca.encodeous.mwx.mwxcompat1_8.MwConstants{
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
