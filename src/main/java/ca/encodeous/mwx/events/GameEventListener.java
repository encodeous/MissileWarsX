package ca.encodeous.mwx.events;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.SoundType;
import ca.encodeous.mwx.data.BoundingRegion;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.game.GameState;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.game.MwTracer;
import ca.encodeous.mwx.util.Msg;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.sk89q.worldedit.math.BlockVector3;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles all Bukkit events for the MissileWars game.
 */
public class GameEventListener implements Listener {

    private final MwPlugin plugin;

    public GameEventListener(MwPlugin plugin) {
        this.plugin = plugin;
    }

    // ======================== Player Join / Quit ========================

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Msg.send(p, Msg.JOIN_MESSAGE);
        MwMatch match = MwGame.getInstance().getMatch();
        if (match != null) {
            match.addPlayer(p, PlayerTeam.LOBBY);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromPlayer(p);
        if (match != null) {
            match.removePlayer(p);
        }
    }

    // ======================== Player Move (pad detection) ========================

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;

        Vector footVec = p.getLocation().toBlockLocation().toVector();

        for (org.bukkit.util.Vector pad : match.getMap().config.autoJoinPads) {
            if (pad.getBlockX() == footVec.getBlockX()
                    && pad.getBlockY() == footVec.getBlockY()
                    && pad.getBlockZ() == footVec.getBlockZ()) {
                match.autoJoinPad(p);
                return;
            }
        }
        for (org.bukkit.util.Vector pad : match.getMap().config.redJoinPads) {
            if (pad.getBlockX() == footVec.getBlockX()
                    && pad.getBlockY() == footVec.getBlockY()
                    && pad.getBlockZ() == footVec.getBlockZ()) {
                match.redJoinPad(p);
                return;
            }
        }
        for (org.bukkit.util.Vector pad : match.getMap().config.greenJoinPads) {
            if (pad.getBlockX() == footVec.getBlockX()
                    && pad.getBlockY() == footVec.getBlockY()
                    && pad.getBlockZ() == footVec.getBlockZ()) {
                match.greenJoinPad(p);
                return;
            }
        }
    }

    // ======================== Player Interact (missiles, fireballs) ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var b = event.getClickedBlock();
            if (b == null) return;

            for (Vector v : match.getMap().config.returnToLobbyBlocks) {
                if (v.equals(b.getLocation().toVector())) {
                    match.addPlayer(p, PlayerTeam.LOBBY);
                    return;
                }
            }

            // Spectate blocks
            for (Vector v : match.getMap().config.spectateBlocks) {
                if (v.equals(b.getLocation().toVector())) {
                    match.addPlayer(p, PlayerTeam.SPECTATOR);
                    return;
                }
            }
        }
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;
        match.handleItemUse(event);
    }

    // ======================== Block Break ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;

        // During map reset, prevent all breaking
        if (match.isBusy()) {
            event.setCancelled(true);
            return;
        }

        Block b = event.getBlock();
        if (!match.getMap().config.boundingBox.isInBounds(b.getLocation().toVector()) && p.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        if(b.getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
            return;
        }
        event.setDropItems(false);

        // Untrack the block
        match.getTracer().untrackBlock(b.getLocation().toVector());

        // Handle nether portal break propagation
        if (b.getType() == Material.NETHER_PORTAL) {
            event.setCancelled(true);
            Set<Block> portalBlocks = MwTracer.propagatePortalBreak(b);
            checkPortalDestruction(match, portalBlocks, List.of(p));
            for (Block pb : portalBlocks) {
                pb.setType(Material.AIR, false);
            }
        }
    }

    private void checkPortalDestruction(MwMatch match, Set<Block> blocks, List<Player> credits) {
        if (blocks.isEmpty()) return;
        BoundingRegion red = match.getMap().config.redPortal;
        BoundingRegion green = match.getMap().config.greenPortal;

        boolean redHit = false, greenHit = false;
        for (Block block : blocks) {
            org.bukkit.util.Vector bv = block.getLocation().toVector();
            if (red != null && red.isInBounds(bv)) redHit = true;
            if (green != null && green.isInBounds(bv)) greenHit = true;
        }

        if (redHit) match.portalBroken(PlayerTeam.RED, credits);
        if (greenHit) match.portalBroken(PlayerTeam.GREEN, credits);
    }

    // ======================== Block Place ========================

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;
        if (match.isBusy()) { event.setCancelled(true); return; }
        if (match.getState() != GameState.PLAYING && p.getGameMode() != GameMode.CREATIVE) { event.setCancelled(true); return; }

        Block b = event.getBlock();

        match.getTracer().trackBlock(b.getLocation().toVector(), p.getUniqueId());
    }

    // ======================== Block Physics ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent event) {
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;

        // Cancel physics during map loading/reset
        if (match.isBusy()) {
            event.setCancelled(true);
            return;
        }

        // Cancel nether portal physics (prevents portal from disappearing)
        if (event.getBlock().getType() == Material.NETHER_PORTAL) {
            event.setCancelled(true);
        }
    }

    // ======================== Piston Events ========================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;
        match.getTracer().updatePistonPush(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;
        // Retraction moves blocks in the opposite direction
        match.getTracer().updatePistonPush(event.getBlocks(), event.getDirection());
    }

    // ======================== Entity Explode ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        MwMatch match = MwGame.fromWorld(event.getLocation().getWorld());
        if (match == null) return;

        Entity entity = event.getEntity();

        // For fireballs: only destroy specific block types
        if (entity instanceof Fireball) {
            event.blockList().removeIf(block -> !isFireballDestroyable(block.getType()));
            return;
        }

        // Determine who caused this explosion
        List<Player> credits = new ArrayList<>();
        if (entity instanceof TNTPrimed tnt) {
            Set<UUID> causeUuids = match.getTracer().lookupEntity(entity);
            for (UUID uuid : causeUuids) {
                Player credited = Bukkit.getPlayer(uuid);
                if (credited != null) credits.add(credited);
            }
        }

        // Find portal blocks in explosion
        List<Block> portalBlocks = new ArrayList<>();
        for (Block b : event.blockList()) {
            if (b.getType() == Material.NETHER_PORTAL) {
                portalBlocks.add(b);
            } else if(b.getType() == Material.TARGET) {
                if(!credits.isEmpty())
                    match.broadcast("&aA Target was blown up by " + Msg.formatNames(match, "&a, ", credits));
                else
                    match.broadcast("&aA Target was blown up by an unknown source.");
                match.playSound(match.getAllPlayers(), SoundType.TICK);
            }
        }

        if (!portalBlocks.isEmpty()) {
            checkPortalDestruction(match, new HashSet<>(portalBlocks), credits);
            for (Block pb : portalBlocks) {
                for (Block b : MwTracer.propagatePortalBreak(pb)) {
                    b.setType(Material.AIR);
                }
            }
        }

        // Untrack all exploded blocks
        for (Block b : event.blockList()) {
            match.getTracer().untrackBlock(b.getLocation().toVector());
        }
    }

    private boolean isFireballDestroyable(Material mat) {
        return switch (mat) {
            case TNT, SLIME_BLOCK, HONEY_BLOCK,
                    PISTON, STICKY_PISTON, PISTON_HEAD,
                    WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA,
                    LIGHT_BLUE_TERRACOTTA, YELLOW_TERRACOTTA, LIME_TERRACOTTA,
                    PINK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA,
                    CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                    BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA,
                    BLACK_TERRACOTTA, TERRACOTTA, REDSTONE_BLOCK, NETHER_PORTAL -> true;
            default -> false;
        };
    }

//    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
//    public void onBlockExplode(BlockExplodeEvent event) {
//        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
//        if (match == null) return;
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        MwMatch match = MwGame.fromWorld(event.getEntity().getWorld());
        if (match == null) return;
        var tr = match.getTracer();

        if (event.getEntity() instanceof TNTPrimed tnt) {
            var b = tnt.getLocation().getBlock();
            var bv = b.getLocation().toVector();
            tr.trackEntity(event.getEntity(), tr.lookupBlock(bv).toArray(UUID[]::new));
            tr.untrackBlock(bv);
        }
        if (event.getEntity() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            tr.trackEntity(event.getEntity(), p.getUniqueId());
        }
        if (event.getEntity() instanceof Snowball sb && sb.getShooter() instanceof Player p) {
            tr.trackEntity(event.getEntity(), p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTntPrime(TNTPrimeEvent event) {
        MwMatch match = MwGame.fromWorld(event.getBlock().getWorld());
        if (match == null) return;

        Block b = event.getBlock();
        var bv = b.getLocation().toVector();
        var tr = match.getTracer();

        var primer = event.getPrimingEntity();
        if (primer != null) {
            tr.trackBlock(bv, tr.lookupEntity(primer).toArray(UUID[]::new));
        }

        var primeBlock = event.getPrimingBlock();
        if (primeBlock != null) {
            tr.trackBlock(bv, tr.lookupBlock(primeBlock.getLocation().toVector()).toArray(UUID[]::new));
        }

        int[] mx = {-1, 1, 0, 0, 0, 0};
        int[] my = {0, 0, -1, 1, 0, 0};
        int[] mz = {0, 0, 0, 0, -1, 1};

        for(int i = 0; i < 6; i++) {
            var nb = b.getWorld().getBlockAt(b.getLocation().add(mx[i], my[i], mz[i]));
            if(nb.isBlockPowered() || nb.getType() == Material.REDSTONE_BLOCK) {
                tr.trackBlock(bv, tr.lookupBlock(nb.getLocation().toVector()).toArray(UUID[]::new));
            }
        }
    }

    // ======================== Death / Respawn ========================

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;

        event.setCancelled(true);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setFallDistance(0);
        p.setVelocity(new Vector(0, 0, 0));
        match.teleportToTeamSpawn(p, match.getTeam(p));
        if (event.deathMessage() == null) return;
        p.getWorld().sendMessage(event.deathMessage());
    }

    // ======================== Damage ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Fireball f) {
            processFireball(f, event.getDamageSource().getDirectEntity());
            return;
        }
        if (!(event.getEntity() instanceof Player p)) return;
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;
        if (!match.isPlayerInMatch(p)) {
            event.setCancelled(true);
        }
    }

    private void processFireball(Fireball f, Entity manipulator) {
        MwMatch match = MwGame.fromWorld(f.getWorld());
        if (match == null || manipulator == null) return;
        var tr = match.getTracer();
        tr.trackEntity(f, tr.lookupEntity(manipulator).toArray(UUID[]::new));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if(event.getHitEntity() instanceof Fireball f) {
            processFireball(f, event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Fireball f) {
            processFireball(f, event.getDamager());
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) return;
        MwMatch match = MwGame.fromPlayer(victim);
        if (match == null) return;

        Player attacker = null;
        Entity damager = event.getDamager();

        switch (damager) {
            case Player pl -> attacker = pl;
            case Projectile proj when proj.getShooter() instanceof Player pl -> attacker = pl;
            case TNTPrimed tntPrimed -> {
                return;
            }
            default -> {
            }
        }

        if (attacker != null) {
            PlayerTeam victimTeam = match.getTeam(victim);
            PlayerTeam attackerTeam = match.getTeam(attacker);
            // Prevent friendly fire
            if (victimTeam == attackerTeam && victimTeam != PlayerTeam.LOBBY) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        var team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return;

        TextColor teamColor = team.color();

        event.renderer(new ChatRenderer() {
            @Override
            public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
                return Component.text()
                        .append(Component.text("<", NamedTextColor.DARK_GRAY))
                        .append(sourceDisplayName.color(teamColor)) // Applies the team color to their name
                        .append(Component.text("> ", NamedTextColor.DARK_GRAY))
                        .append(message.color(NamedTextColor.WHITE)) // The actual chat message
                        .build();
            }
        });
    }

    // ======================== Food ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;
        event.setCancelled(true);
        event.getEntity().setFoodLevel(20);
    }

    // ======================== Item Drop ========================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        MwMatch match = MwGame.fromPlayer(p);
        if (match == null) return;
        if (p.getGameMode() == GameMode.CREATIVE) return;

        // Only allow dropping arrows
        if (event.getItemDrop().getItemStack().getType() == Material.ARROW) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        MwMatch match = MwGame.fromPlayer(event.getPlayer());
        if (match == null) return;
        event.setCancelled(true);
    }
}
