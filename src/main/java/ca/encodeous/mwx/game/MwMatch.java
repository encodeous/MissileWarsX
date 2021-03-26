package ca.encodeous.mwx.game;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.ItemConfig;
import ca.encodeous.mwx.config.ItemType;
import ca.encodeous.mwx.config.PluginConfig;
import ca.encodeous.mwx.config.SoundType;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.structure.StructurePlacer;
import ca.encodeous.mwx.structure.StructureTemplate;
import ca.encodeous.mwx.structure.StructureUtils;
import ca.encodeous.mwx.util.Msg;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Central game state manager for one active MissileWars match.
 */
public class MwMatch {

    // Shared scoreboard teams (initialized by MwPlugin)
    public static Team teamRed;
    public static Team teamGreen;
    public static Team teamSpectator;
    public static Team teamLobby;

    private final MwMap map;
    private volatile GameState state = GameState.WAITING;

    private final Set<Player> redPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> greenPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> spectators = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> lobbyPlayers = Collections.synchronizedSet(new HashSet<>());

    private final MwTracer tracer = new MwTracer();
    private final ItemDistributor itemDistributor;

    private BukkitTask startCountdownTask;
    private BukkitTask endCountdownTask;
    private int startCountdownRemaining;

    private PlayerTeam winningTeam = null;

    public MwMatch(MwMap map) {
        this.map = map;
        this.itemDistributor = new ItemDistributor(this);
    }

    // ======================== Player Management ========================

    /**
     * Add or move a player to a team. Handles all state transitions.
     */
    public void addPlayer(Player p, PlayerTeam assignedTeam) {
        if (getTeam(p) == assignedTeam) return;

        if (assignedTeam == PlayerTeam.RED || assignedTeam == PlayerTeam.GREEN) {
            PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
            Set<Player> targetSet = assignedTeam == PlayerTeam.RED ? redPlayers : greenPlayers;
            if (targetSet.size() >= config.maxTeamSize) {
                Msg.send(p, Msg.TEAM_FULL);
                return;
            }
        }

        removeFromTeamSets(p);

        switch (assignedTeam) {
            case RED -> redPlayers.add(p);
            case GREEN -> greenPlayers.add(p);
            case SPECTATOR -> spectators.add(p);
            default -> lobbyPlayers.add(p);
        }

        if (isPlayerInMatch(p)) {
            playSound(getAllPlayers(), SoundType.LEAVE, p.getLocation());
        }

        setPlayerScoreboardTeam(p, assignedTeam);
        clearPlayer(p);
        teleportToTeamSpawn(p, assignedTeam);
        setPlayerGamemode(p, assignedTeam);
        announceTeam(p, assignedTeam);

        if (assignedTeam == PlayerTeam.RED || assignedTeam == PlayerTeam.GREEN) {
            if (state == GameState.PLAYING) {
                equipPlayer(p, assignedTeam);
            }
            playSound(getAllPlayers(), SoundType.JOIN, p.getLocation());
        }

        if (state == GameState.WAITING || state == GameState.STARTING) {
            checkReadyState();
        }
    }

    public void removePlayer(Player p) {
        removeFromTeamSets(p);
        if (teamLobby != null) teamLobby.removeEntry(p.getName());
        if (teamRed != null) teamRed.removeEntry(p.getName());
        if (teamGreen != null) teamGreen.removeEntry(p.getName());
        if (teamSpectator != null) teamSpectator.removeEntry(p.getName());

        if (state == GameState.STARTING) {
            checkReadyState();
        }
    }

    private void removeFromTeamSets(Player p) {
        redPlayers.remove(p);
        greenPlayers.remove(p);
        spectators.remove(p);
        lobbyPlayers.remove(p);
    }

    public PlayerTeam getTeam(Player p) {
        if (redPlayers.contains(p)) return PlayerTeam.RED;
        if (greenPlayers.contains(p)) return PlayerTeam.GREEN;
        if (spectators.contains(p)) return PlayerTeam.SPECTATOR;
        if (lobbyPlayers.contains(p)) return PlayerTeam.LOBBY;
        return PlayerTeam.NONE;
    }

    public boolean isPlayerInMatch(Player p) {
        return getTeam(p) == PlayerTeam.RED || getTeam(p) == PlayerTeam.GREEN;
    }

    public Collection<Player> getAllPlayers() {
        Set<Player> all = new HashSet<>();
        all.addAll(redPlayers);
        all.addAll(greenPlayers);
        all.addAll(spectators);
        all.addAll(lobbyPlayers);
        return all;
    }

    public Collection<Player> getAllActivePlayers() {
        Set<Player> all = new HashSet<>();
        all.addAll(redPlayers);
        all.addAll(greenPlayers);
        return all;
    }

    public Set<Player> getRedPlayers() { return new HashSet<>(redPlayers); }
    public Set<Player> getGreenPlayers() { return new HashSet<>(greenPlayers); }
    public Set<Player> getSpectators() { return new HashSet<>(spectators); }
    public Set<Player> getLobbyPlayers() { return new HashSet<>(lobbyPlayers); }

    private void equipPlayer(Player p, PlayerTeam team) {
        p.getInventory().clear();

        // Give base items from config (arrow, sword, bow)
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        for (ItemConfig item : config.items) {
            if (item.giveAtBeginning) {
                p.getInventory().addItem(item.createItemStack(MwPlugin.getInstance()));
            }
        }

        // Give leather armor tinted red or green
        Color armorColor = team == PlayerTeam.RED ? Color.RED : Color.GREEN;
        p.getInventory().setChestplate(createArmour(Material.LEATHER_CHESTPLATE, armorColor));
        p.getInventory().setLeggings(createArmour(Material.LEATHER_LEGGINGS, armorColor));
        p.getInventory().setBoots(createArmour(Material.LEATHER_BOOTS, armorColor));

        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setLevel(0);
        p.setExp(0f);
    }

    private ItemStack createArmour(Material mat, Color color) {
        ItemStack stack = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setEnchantmentGlintOverride(false);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private void clearPlayer(Player p) {
        p.getInventory().clear();
        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setLevel(0);
        p.setExp(0f);
        p.setFireTicks(0);
        p.setFallDistance(0);
    }

    private void announceTeam(Player p, PlayerTeam team) {
        Msg.broadcast(getAllPlayers(),  switch(team) {
            case RED -> "&c" + p.getName() + " joined the red team.";
            case GREEN -> "&a" + p.getName() + " joined the Green team.";
            case SPECTATOR -> "&9" + p.getName() + " started spectating";
            default -> "&7" + p.getName() + " joined the lobby.";
        });
    }

    public void teleportToTeamSpawn(Player p, PlayerTeam team) {
        if (team == PlayerTeam.SPECTATOR) return;
        Location loc;
        if (state == GameState.PLAYING) {
            loc = switch (team) {
                case RED -> map.getRedSpawn();
                case GREEN -> map.getGreenSpawn();
                default -> map.getLobbySpawn();
            };
        } else {
            loc = switch (team) {
                case RED -> map.getRedLobbyLocation();
                case GREEN -> map.getGreenLobbyLocation();
                default -> map.getLobbySpawn();
            };
        }

        if (loc != null && loc.getWorld() != null) {
            p.teleport(loc);
        }
    }

    private void setPlayerGamemode(Player p, PlayerTeam team) {
        if (team == PlayerTeam.SPECTATOR || state == GameState.ENDING) {
            p.setGameMode(GameMode.SPECTATOR);
        }
        else if (team == PlayerTeam.LOBBY) {
            p.setGameMode(GameMode.ADVENTURE);
        } else {
            p.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void setPlayerScoreboardTeam(Player p, PlayerTeam team) {
        if (teamRed != null) teamRed.removeEntry(p.getName());
        if (teamGreen != null) teamGreen.removeEntry(p.getName());
        if (teamSpectator != null) teamSpectator.removeEntry(p.getName());
        if (teamLobby != null) teamLobby.removeEntry(p.getName());

        switch (team) {
            case RED -> { if (teamRed != null) teamRed.addEntry(p.getName()); }
            case GREEN -> { if (teamGreen != null) teamGreen.addEntry(p.getName()); }
            case SPECTATOR -> { if (teamSpectator != null) teamSpectator.addEntry(p.getName()); }
            default -> { if (teamLobby != null) teamLobby.addEntry(p.getName()); }
        }
    }

    // ======================== Pad Interaction ========================

    public void autoJoinPad(Player p) {
        // Balance teams
        int redCount = redPlayers.size();
        int greenCount = greenPlayers.size();
        PlayerTeam assignTo = redCount <= greenCount ? PlayerTeam.RED : PlayerTeam.GREEN;
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        if (redPlayers.size() >= config.maxTeamSize && greenPlayers.size() >= config.maxTeamSize) {
            Msg.send(p, Msg.GAME_FULL);
            return;
        }
        addPlayer(p, assignTo);
    }

    public void redJoinPad(Player p) {
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        if (redPlayers.size() >= config.maxTeamSize) {
            Msg.send(p, Msg.GAME_FULL);
            return;
        }
        addPlayer(p, PlayerTeam.RED);
    }

    public void greenJoinPad(Player p) {
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        if (greenPlayers.size() >= config.maxTeamSize) {
            Msg.send(p, Msg.GAME_FULL);
            return;
        }
        addPlayer(p, PlayerTeam.GREEN);
    }

    // ======================== Game Lifecycle ========================

    public void checkReadyState() {
        if (state != GameState.WAITING && state != GameState.STARTING) return;
        boolean enoughPlayers = !(redPlayers.isEmpty() && greenPlayers.isEmpty());
        if (enoughPlayers && state == GameState.WAITING) {
            startCountdown();
        } else if (!enoughPlayers && state == GameState.STARTING) {
            cancelCountdown();
        }
    }

    public void startCountdown() {
        if (state == GameState.STARTING) return;
        state = GameState.STARTING;
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        startCountdownRemaining = config.startCountdownSeconds;
        startCountdownTask = Bukkit.getScheduler().runTaskTimer(MwPlugin.getInstance(), () -> {
            if (startCountdownRemaining <= 0 && state == GameState.STARTING) {
                if (startCountdownTask != null) startCountdownTask.cancel();
                beginGame();
            } else {
                for(Player p : getAllPlayers()) {
                    int s = startCountdownRemaining;
                    String msg = s == 1
                            ? String.format(Msg.STARTING_GAME, s)
                            : String.format(Msg.STARTING_GAME_PLURAL, s);
                    Msg.actionBar(p, msg);
                    playSound(getAllPlayers(), SoundType.TICK);
                }
            }
            startCountdownRemaining--;
        }, 0L, 20L);
    }

    public void cancelCountdown() {
        if (startCountdownTask != null) {
            startCountdownTask.cancel();
            startCountdownTask = null;
        }
        state = GameState.WAITING;
    }

    public void beginGame() {
        if (startCountdownTask != null) {
            startCountdownTask.cancel();
            startCountdownTask = null;
        }
        state = GameState.PLAYING;
        onGameStart();
    }

    private void onGameStart() {
        // Equip and teleport all active players
        for (Player p : redPlayers) {
            equipPlayer(p, PlayerTeam.RED);
            teleportToTeamSpawn(p, PlayerTeam.RED);
        }
        for (Player p : greenPlayers) {
            equipPlayer(p, PlayerTeam.GREEN);
            teleportToTeamSpawn(p, PlayerTeam.GREEN);
        }
        broadcast(Msg.GAME_STARTED);
        itemDistributor.start();
    }

    public void playSound(Collection<Player> targets, SoundType type, Location loc) {
        var sounds = MwPlugin.getInstance().getGame().getConfig().eventSounds.get(type);
        for (var sound : sounds) {
            for (var p : targets) {
                p.playSound(loc, sound, 1.0f, 1);
            }
        }
    }

    public void playSound(Collection<Player> targets, SoundType type) {
        var sounds = MwPlugin.getInstance().getGame().getConfig().eventSounds.get(type);
        for (var sound : sounds) {
            for (var p : targets) {
                p.playSound(p.getLocation(), sound, 1.0f, 1);
            }
        }
    }

    public void playSound(Collection<Player> targets, ItemType type, Location loc) {
        var sounds = MwPlugin.getInstance().getGame().getConfig().deploySounds.get(type);
        for (var sound : sounds) {
            for (var p : targets) {
                p.playSound(loc, sound, 1.0f, 1);
            }
        }
    }

    /**
     * Called when a portal is broken. isRedPortal = true means red team's portal was broken.
     */
    public void portalBroken(PlayerTeam team, List<Player> credits) {
        if (state != GameState.PLAYING) return;

        if (credits.isEmpty()) {
            broadcast(String.format(Msg.PORTAL_BROKEN_UNKNOWN, team.getName()));
        } else {
            broadcast(String.format(Msg.PORTAL_BROKEN, team.getName(),
                    Msg.formatNames(this, "&c, ", credits)));
        }

        Set<Player> winners, losers;

        if (team == PlayerTeam.RED) {
            winners = getGreenPlayers();
            losers = getRedPlayers();
        } else {
            winners = getRedPlayers();
            losers = getGreenPlayers();
        }

        for(var p : getAllActivePlayers()) {
            addPlayer(p, PlayerTeam.SPECTATOR);
        }

        if (winningTeam != null) {
            winningTeam = PlayerTeam.NONE;
            playSound(getAllPlayers(), SoundType.DRAW);
            broadcast(Msg.DRAW);
            for(var p : getAllPlayers()) {
                p.showTitle(Title.title(Msg.component(Msg.DRAW_TITLE), Msg.component(Msg.DRAW_SUBTITLE)));
            }
            endGame();
        } else {
            winningTeam = team.getOpponent();
            playSound(losers, SoundType.LOSE);
            playSound(winners, SoundType.WIN);
            for(var p : winners) {
                p.showTitle(Title.title(Msg.component(Msg.TEAM_WIN_TITLE), Msg.component(Msg.TEAM_WIN_SUBTITLE)));
            }
            for(var p : losers) {
                p.showTitle(Title.title(Msg.component(Msg.TEAM_LOSE_TITLE), Msg.component(Msg.TEAM_LOSE_SUBTITLE)));
            }
            PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
            endCountdownTask = new BukkitRunnable() {
                private int endCountdownRemaining = config.drawCheckSeconds;
                @Override
                public void run() {
                    if (endCountdownRemaining <= 0) {
                        this.cancel();
                        endGame();
                        return;
                    }
                    for(Player p : getAllPlayers()) {
                        Msg.actionBar(p, String.format(Msg.DRAW_CHECK, endCountdownRemaining));
                    }
                    playSound(getAllPlayers(), SoundType.TICK);
                    endCountdownRemaining--;
                }
            }.runTaskTimer(MwPlugin.getInstance(), 0L, 20L);
        }
    }

    public void endGame() {
        if (state == GameState.ENDING) return;
        state = GameState.ENDING;
        itemDistributor.stop();
        if (startCountdownTask != null) { startCountdownTask.cancel(); startCountdownTask = null; }
        if (endCountdownTask != null) { endCountdownTask.cancel(); endCountdownTask = null; }
        scheduleReset();
    }

    public void scheduleReset() {
        broadcast(Msg.GAME_RESET);
        Bukkit.getScheduler().runTaskLater(MwPlugin.getInstance(), this::performReset, 5 * 20L);
    }

    public void performReset() {
        tracer.clear();

        // Move all players to lobby spawn while map resets
        for (Player p : getAllPlayers()) {
            clearPlayer(p);
            setPlayerScoreboardTeam(p, PlayerTeam.LOBBY);
            Location lobby = map.getLobbySpawn();
            if (lobby != null) p.teleport(lobby);
        }

        map.resetMap(this::onMapReady);
    }

    public void onMapReady() {
        // Re-initialize match state
        Set<Player> allPlayers = new HashSet<>(getAllPlayers());
        redPlayers.clear();
        greenPlayers.clear();
        spectators.clear();
        lobbyPlayers.clear();
        winningTeam = null;
        state = GameState.WAITING;

        // Re-add all online players to lobby
        for (Player p : allPlayers) {
            if (p.isOnline()) {
                lobbyPlayers.add(p);
                setPlayerScoreboardTeam(p, PlayerTeam.LOBBY);
                clearPlayer(p);
                Location lobby = map.getLobbySpawn();
                if (lobby != null) p.teleport(lobby);
            }
        }
    }

    public void handleItemUse(PlayerInteractEvent event) {
        if (getState() != GameState.PLAYING) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        var plugin = MwPlugin.getInstance();
        String itemId = ItemConfig.getItemId(plugin, item);
        if (itemId == null) return;

        PluginConfig config = plugin.getGame().getConfig();
        ItemConfig itemCfg = config.getItem(itemId);
        if (itemCfg == null) return;
        var p = event.getPlayer();
        var world = event.getPlayer().getWorld();

        if (state != GameState.PLAYING || !isPlayerInMatch(p)) {
            event.setCancelled(true);
            return;
        }

        switch(itemCfg.itemType) {
            case SHIELD -> {
                event.setCancelled(true);
                deployShield(p, itemCfg);
                item.setAmount(item.getAmount() - 1);
            }
            case FIREBALL -> {
                event.setCancelled(true);
                var block = event.getClickedBlock();
                if (block == null) return;
                if (summonFireball(p, block.getLocation())) {
                    playSound(getAllPlayers(), ItemType.FIREBALL, block.getLocation());
                    item.setAmount(item.getAmount() - 1);
                }
            }
            case ITEM -> {
            }
            case MISSILE -> {
                event.setCancelled(true);
                var block = event.getClickedBlock();
                if (block == null) return;
                if (deployMissile(p, block, itemCfg)) {
                    playSound(getAllPlayers(), itemCfg.itemType, block.getLocation());
                    item.setAmount(item.getAmount() - 1);
                }
            }
            case GUNBLADE -> {
            }
        }
    }

    public void deployShield(Player p, ItemConfig itemCfg) {
        PlayerTeam team = getTeam(p);

        Snowball shield = p.launchProjectile(Snowball.class, null, (sb) -> {
            sb.setShooter(p);
        });
        playSound(getAllPlayers(), SoundType.SHIELD_THROW, p.getLocation());

        Bukkit.getScheduler().runTaskLater(MwPlugin.getInstance(), () -> {
            if (!shield.isDead()) {
                Location loc = shield.getLocation();
                String schematicId = itemCfg.schematicId;
                playSound(getAllPlayers(), SoundType.SHIELD_DEPLOY, loc);
                if (schematicId != null) {
                    StructureTemplate template =
                            MwGame.getInstance()
                                    .getStructureRegistry()
                                    .get(schematicId);
                    if (template != null) {
                        ArrayList<Vector> blocks =
                                StructureUtils.getTemplateBlocks(template, loc.toVector(), team);
                        if (StructureUtils.checkCanSpawn(team, blocks, loc.getWorld(), true, map.config)) {
                            StructurePlacer.placeStructure(template, loc, team, p, this);
                        } else {
                            Msg.actionBar(p, Msg.DEPLOY_FAILED);
                            playSound(List.of(p), SoundType.FAIL);
                        }
                    }
                }
                shield.remove();
            }
        }, 20L);
    }

    public boolean deployMissile(Player p, Block targetBlock, ItemConfig itemCfg) {
        StructureTemplate template = MwPlugin
                .getInstance()
                .getGame()
                .getStructureRegistry()
                .get(itemCfg.schematicId);
        if (template == null) {
            p.sendMessage(Msg.component("&cStructure template not found: " + itemCfg.schematicId));
            return false;
        }
        var loc = targetBlock.getLocation();
        ArrayList<Vector> blocks =
                StructureUtils.getTemplateBlocks(template, loc.toVector(), getTeam(p));
        if (StructureUtils.checkCanSpawn(getTeam(p), blocks, loc.getWorld(), false, map.config)) {
            return StructurePlacer.placeStructure(template, loc, getTeam(p), p, this);
        } else {
            Msg.actionBar(p, Msg.DEPLOY_FAILED);
            playSound(List.of(p), SoundType.FAIL);
        }
        return false;
    }

    public boolean summonFireball(Player p, Location block) {
        var world = p.getWorld();
        var pos = block.add(0.5, 2, 0.5);
        if (world.getBlockAt(pos).isSolid()) return false; // don't spawn in solid block

        Fireball e = world.spawn(pos, Fireball.class, fb -> {
            fb.setYield(1f);
            fb.setShooter(p);
            fb.setIsIncendiary(true);
            fb.setVelocity(new Vector(0, 0, 0));
        });
        return true;
    }

    public void broadcast(String message) {
        Msg.broadcast(getAllPlayers(), message);
    }

    public GameState getState() { return state; }
    public MwMap getMap() { return map; }
    public MwTracer getTracer() { return tracer; }
    public ItemDistributor getItemDistributor() { return itemDistributor; }

    public boolean isBusy() { return map.busy; }
}
