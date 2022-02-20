package ca.encodeous.mwx.item;

import ca.encodeous.mwx.core.game.CoreGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class TntThrowerItem extends SpecialItem {

    public Material getItem() {
        return Material.CARROT_ON_A_STICK;
    }

    public int getData() {
        return 2;
    }

    public void onUse(PlayerInteractEvent e) {
        double yaw = e.getPlayer().getLocation().getYaw();
        double pitch = e.getPlayer().getLocation().getPitch();
        double r = Math.PI / 180;
        double h = -Math.cos(-pitch * r);
        double xMov = Math.sin(-yaw * r - Math.PI) * h;
        double yMov = Math.sin(-pitch * r);
        double zMov = Math.cos(-yaw * r - Math.PI) * h;

        xMov *= 2;
        yMov *= 2;
        zMov *= 2;

        Location loc = e.getPlayer().getLocation();
        TNTPrimed tnt = e.getPlayer().getWorld().spawn(loc.add(xMov, yMov + e.getPlayer().getEyeHeight() - 0.25, zMov), TNTPrimed.class);
        tnt.setFuseTicks(200);
        tnt.setVelocity(new Vector(xMov, yMov, zMov));
        tnt.setYield(1);
        tnt.setMetadata("explodeOnImpact", new FixedMetadataValue(CoreGame.Instance.mwPlugin, true));
    }

}
