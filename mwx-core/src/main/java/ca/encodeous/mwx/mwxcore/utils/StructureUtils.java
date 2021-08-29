package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.soundengine.SoundType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class StructureUtils {
    public static boolean CheckCanSpawn(PlayerTeam team, ArrayList<Vector> blocks, World world, boolean isShield){
        // referenced from OpenMissileWars
        int threshold = 0;
        Bounds bound = new Bounds();
        for(Vector vec : blocks){
            bound.stretch(vec);
        }
        for(int i = bound.getMinX(); i <= bound.getMaxX(); i++){
            for(int j = bound.getMinY(); j <= bound.getMaxY(); j++){
                for(int k = bound.getMinZ(); k <= bound.getMaxZ(); k++){
                    Vector vec = new Vector(i, j, k);
                    Block block = world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
                    Material mat = block.getType();
                    if(mat == Material.OBSIDIAN || mat == Material.BEDROCK
                            || mat == CoreGame.GetImpl().GetPortalMaterial()
                            || mat == Material.BARRIER) return false;
                    if(IsInProtectedRegion(vec)) return false;
                    if(isShield) continue;
                    boolean crossMid;
                    if (team == PlayerTeam.Red) {
                        crossMid = vec.getBlockZ() >= 0;
                    } else {
                        crossMid = vec.getBlockZ() <= 0;
                    }

                    boolean isSameTeamBlock = CoreGame.GetImpl().GetStructureManager().IsBlockOfTeam(team, block);
                    if(!isSameTeamBlock && crossMid){
                        threshold--;
                    }
                    if(isSameTeamBlock){
                        threshold++;
                    }
                    if(CoreGame.GetImpl().GetStructureManager().IsBlockOfTeam(PlayerTeam.None, block) && !crossMid){
                        threshold++;
                    }
                }
            }
        }
        return threshold < 5;
    }

    public static boolean IsInProtectedRegion(Vector vec){
        if(vec.getBlockX() <= -72) return true;
        if(vec.getBlockY() <= 0) return true;
        if(vec.getBlockZ() >= 120) return true;
        if(vec.getBlockZ() <= -120) return true;
        if(vec.getBlockY() >= 150) return true;
        return false;
    }
    public static void LaunchShield(Player p, String mwItemId, Ref<Boolean> cancel, Ref<Boolean> use, boolean isRed, MissileWarsMatch match) {
        if(mwItemId.equals(MissileWarsCoreItem.SHIELD.getValue())){
            final Snowball shield = p.launchProjectile(Snowball.class);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, new Runnable() {
                public void run() {
                    if(!shield.isDead()){
                        boolean result = CoreGame.GetImpl().GetStructureManager().SpawnShield(shield.getLocation().toVector(), shield.getWorld(), isRed);
                        if(!result){
                            CoreGame.GetImpl().PlaySound(p, SoundType.ITEM_NOT_GIVEN);
                            MissileWarsMatch.SendCannotPlaceMessage(p);
                        }else{
                            CoreGame.GetImpl().PlaySound(shield.getLocation(), SoundType.SHIELD);
                        }
                        shield.remove();
                    }
                }
            }, 20L);
            cancel.val = true;
            use.val = true;
        }
    }
}
