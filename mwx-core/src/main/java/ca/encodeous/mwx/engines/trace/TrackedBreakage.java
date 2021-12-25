package ca.encodeous.mwx.engines.trace;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Utils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TrackedBreakage extends TrackedBlock {
    private double time;
    private double timeBetweenBreakage;
    private int oldAnimation;
    private int damage = 0;
    private int entityId;
    private MissileWarsMatch match;
    public Material blockMaterial;
    private ScheduledFuture<?> task = null;
    private Player lastPlayer = null;
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public TrackedBreakage(Block block, double time, MissileWarsMatch curMatch) {
        blockMaterial = block.getType();
        this.time = time;
        entityId = new Random().nextInt();
        timeBetweenBreakage = this.time / 10.0;
        match = curMatch;
    }

    public void clear(){
        if(task != null){
            task.cancel(false);
        }
        damage = 0;
        showBreakage();
    }

    public void startBreak(Player p){
        lastPlayer = p;
        if(timeBetweenBreakage == 0){
            if(task != null){
                task.cancel(false);
            }
            damage = 0;
            breakBlock();
            return;
        }
        clear();
        task = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(isBroken()){
                    clear();
                    breakBlock();
                }else{
                    showBreakage();
                }
                damage += (int)(10 * (timeBetweenBreakage / time));
            }
        }, 0, (int)timeBetweenBreakage, TimeUnit.MILLISECONDS);
    }

    public void cancelBreak(){
        clear();
    }

    public void showBreakage(){
        sendBreakPacket(getAnimation());
    }

    public boolean isBroken() {
        return getAnimation() >= 10;
    }

    public void breakBlock() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, new Runnable() {
            @Override
            public void run() {
                destroyBlockObject();
                Location loc = Utils.LocationFromVec(Position, match.Map.MswWorld);
                Block block = loc.getBlock();
                Material mat = block.getType();
                if(match.EventHandler.BlockBreakEvent(lastPlayer, block)){
                    block.breakNaturally();
                    if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_13.getValue()){
                        loc.getWorld().playEffect(loc, Effect.STEP_SOUND, mat);
                    }
                    else{
                        loc.getWorld().playEffect(loc, Effect.STEP_SOUND, mat.getId());
                    }
                }
            }
        }, 0);
    }

    public void destroyBlockObject() {
        sendBreakPacket(-1);
        match.Tracer.RemoveBreak(this.Position);
    }

    public int getAnimation() {
        return damage - 1;
    }

    public void sendBreakPacket(int animation) {
        PacketContainer packet1 = CoreGame.Instance.protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet1.getBlockPositionModifier().write(0, getBlockPosition());
        packet1.getIntegers().write(0, entityId);
        packet1.getIntegers().write(1, animation);
        CoreGame.Instance.protocolManager.broadcastServerPacket(packet1, Utils.LocationFromVec(Position, match.Map.MswWorld), 120);
    }


    private BlockPosition getBlockPosition() {
        return new BlockPosition(Position.getBlockX(), Position.getBlockY(), Position.getBlockZ());
    }
}
