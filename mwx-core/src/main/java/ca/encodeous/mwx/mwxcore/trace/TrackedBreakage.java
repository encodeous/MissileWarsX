package ca.encodeous.mwx.mwxcore.trace;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MCVersion;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.utils.Utils;
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

public class TrackedBreakage extends TrackedBlock {
    private int time;
    private int oldAnimation;
    private double damage = 0;
    private int entityId;
    private MissileWarsMatch match;
    public Material blockMaterial;
    private int taskId = -1;
    private Player lastPlayer = null;

    public TrackedBreakage(Block block, int time, MissileWarsMatch curMatch) {
        blockMaterial = block.getType();
        this.time = time;
        entityId = new Random().nextInt();
        match = curMatch;
    }

    public void clear(){
        if(taskId != -1){
            Bukkit.getScheduler().cancelTask(taskId);
        }
        damage = 0;
        showBreakage();
    }

    public void startBreak(Player p){
        lastPlayer = p;
        clear();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoreGame.Instance.mwPlugin, new Runnable() {
            @Override
            public void run() {
                if(isBroken()){
                    breakBlock();
                    clear();
                }else{
                    showBreakage();
                }
                damage += time / 10.0;
            }
        }, 0, time / 10);
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

    public void destroyBlockObject() {
        sendBreakPacket(-1);
        match.Tracer.RemoveBreak(this.Position);
    }

    public int getAnimation() {
        return (int) (damage / time * 11) - 1;
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
