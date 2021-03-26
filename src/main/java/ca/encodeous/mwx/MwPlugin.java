package ca.encodeous.mwx;

import ca.encodeous.mwx.commands.MwxCommand;
import ca.encodeous.mwx.events.GameEventListener;
import ca.encodeous.mwx.events.ProtocolListener;
import ca.encodeous.mwx.game.MwMatch;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entry point for MissileWarsX 2.
 */
public class MwPlugin extends JavaPlugin {

    private static MwPlugin instance;
    private MwGame game;
    private ProtocolListener listener;
    public GameEventListener gameEvents;

    public void saveResourceFolder(String folderPath, boolean replace) {
        folderPath = folderPath.replace('\\', '/');
        if (folderPath.startsWith("/")) {
            folderPath = folderPath.substring(1);
        }
        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        try (JarFile jar = new JarFile(this.getFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(folderPath) && !entry.isDirectory()) {
                    this.saveResource(entryName, replace);
                }
            }
        } catch (IOException e) {
            this.getLogger().severe("Failed to extract resources from folder: " + folderPath);
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            saveDefaultConfig();
            saveResourceFolder("maps", false);
            saveResourceFolder("structures", false);
        }

        setupScoreboardTeams();

        MwGame.initialize(this);
        game = MwGame.getInstance();

        // Register events
        gameEvents = new GameEventListener(this);
        Bukkit.getPluginManager().registerEvents(gameEvents, this);

        // Register ProtocolLib listener
        listener = new ProtocolListener(this);
        listener.register();

        // Register Brigadier-style /mwx command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            MwxCommand.register(this, commands);
        });

        getLogger().info("MissileWarsX enabled.");
    }

    @Override
    public void onDisable() {
        listener.unregister();
        game = null;
        instance = null;
        getLogger().info("MissileWarsX disabled.");
    }

    /**
     * Create or reset the four shared scoreboard teams.
     */
    private void setupScoreboardTeams() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();

        MwMatch.teamRed = getOrCreateTeam(board, "mwx-red");
        MwMatch.teamGreen = getOrCreateTeam(board, "mwx-green");
        MwMatch.teamSpectator = getOrCreateTeam(board, "mwx-spectator");
        MwMatch.teamLobby = getOrCreateTeam(board, "mwx-lobby");

        // Red team
        MwMatch.teamRed.setColor(ChatColor.RED);
        MwMatch.teamRed.setPrefix(ChatColor.RED + "");
        MwMatch.teamRed.setAllowFriendlyFire(false);
        MwMatch.teamRed.setCanSeeFriendlyInvisibles(true);
        MwMatch.teamRed.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        MwMatch.teamRed.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        // Green team
        MwMatch.teamGreen.setColor(ChatColor.GREEN);
        MwMatch.teamGreen.setPrefix(ChatColor.GREEN + "");
        MwMatch.teamGreen.setAllowFriendlyFire(false);
        MwMatch.teamGreen.setCanSeeFriendlyInvisibles(true);
        MwMatch.teamGreen.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        MwMatch.teamGreen.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        // Spectator team
        MwMatch.teamSpectator.setColor(ChatColor.DARK_BLUE);
        MwMatch.teamSpectator.setPrefix(ChatColor.DARK_BLUE + "");
        MwMatch.teamSpectator.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        MwMatch.teamSpectator.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);

        // Lobby team
        MwMatch.teamLobby.setColor(ChatColor.GRAY);
        MwMatch.teamLobby.setPrefix(ChatColor.GRAY + "");
        MwMatch.teamLobby.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        MwMatch.teamLobby.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    private Team getOrCreateTeam(Scoreboard board, String name) {
        Team existing = board.getTeam(name);
        if (existing != null) {
            existing.getEntries().forEach(existing::removeEntry);
            return existing;
        }
        return board.registerNewTeam(name);
    }

    public static MwPlugin getInstance() {
        return instance;
    }

    public MwGame getGame() {
        return game;
    }
}
