package ca.encodeous.mwx.events;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.PluginConfig;
import ca.encodeous.mwx.game.MwMatch;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles ProtocolLib packets for fast block breaking.
 * Intercepts PLAYER_DIGGING packets to allow near-instant removal of configured block types.
 */
public class ProtocolListener {

    private final MwPlugin plugin;
    private final Map<String, BukkitTask> pendingBreaks = new HashMap<>();
    private PacketListener listener;
    private Random rng = new Random();

    public ProtocolListener(MwPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        listener = new PacketAdapter(plugin, ListenerPriority.NORMAL,
                            PacketType.Play.Client.BLOCK_DIG) {
                        @Override
                        public void onPacketReceiving(PacketEvent event) {
                            handleDiggingPacket(event);
                        }
                    };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

    private void handleDiggingPacket(PacketEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;
        if(p.getGameMode() == GameMode.CREATIVE) return;

        EnumWrappers.PlayerDigType action = event.getPacket()
                .getPlayerDigTypes().read(0);

        BlockPosition rawPos =
                event.getPacket().getBlockPositionModifier().read(0);
        if (rawPos == null) return;
        if (rawPos.toVector().distance(p.getLocation().toVector()) > 10) return; // dont let the player break blocks too far away

        Block block = p.getWorld().getBlockAt(rawPos.getX(), rawPos.getY(), rawPos.getZ());
        String blockKey = rawPos.getX() + "," + rawPos.getY() + "," + rawPos.getZ();

        PluginConfig config = plugin.getGame().getConfig();

        if (action == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            String matName = block.getType().name();
            Integer breakTicks = config.breakSpeeds.get(matName);

            if (breakTicks != null) {
                var task = new BukkitRunnable() {
                    final int tickStart = MwPlugin.getInstance().getServer().getCurrentTick();
                    final int eid = rng.nextInt();
                    @Override
                    public void run() {
                        var completion = (double)(MwPlugin.getInstance().getServer().getCurrentTick() - tickStart) / breakTicks;
                        if(completion >= 1) {
                            this.cancel();
                            // break block
                            pendingBreaks.remove(blockKey);
                            if (block.getType() != Material.AIR) {
                                var evt = new BlockBreakEvent(block, p);
                                plugin.gameEvents.onBreak(evt);
                                if(!evt.isCancelled()) {
                                    block.breakNaturally(true);
                                }
                            }
                            sendBreakPacket(rawPos, getTaskId(), 10, p);
                        } else {
                            sendBreakPacket(rawPos, getTaskId(), (int) (completion * 10), p);
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);
                BukkitTask old = pendingBreaks.put(blockKey, task);
                if (old != null) old.cancel();
            }
        } else if (action == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK
                || action == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
            BukkitTask task = pendingBreaks.remove(blockKey);
            if (task != null) {
                task.cancel();
                sendBreakPacket(rawPos, task.getTaskId(), 10, p);
            }

        }
    }

    public void sendBreakPacket(BlockPosition pos, int entityId, int progress, Player p) {
        var man = ProtocolLibrary.getProtocolManager();
        var packet = man.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getBlockPositionModifier().write(0, pos);
        packet.getIntegers().write(0, entityId);
        packet.getIntegers().write(1, progress);
        man.broadcastServerPacket(packet, pos.toLocation(p.getWorld()), 25);
    }
}
