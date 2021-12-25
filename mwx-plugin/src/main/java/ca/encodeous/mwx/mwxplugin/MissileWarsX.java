package ca.encodeous.mwx.mwxplugin;

import ca.encodeous.mwx.commands.*;
import ca.encodeous.mwx.mwxcompat1_13.MissileWars1_13;
import ca.encodeous.mwx.mwxcompat1_8.MissileWars1_8;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import com.keenant.tabbed.skin.SkinFetcher;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class MissileWarsX extends JavaPlugin {
    public static MissileWarsX Instance;
    public MissileWarsImplementation mwImpl;
    public CoreGame MissileWars;
    private Logger logger = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        LogManager.getLogManager().getLogger("").setFilter(new ConsoleFilter());
        Bukkit.getServer().getPluginManager().registerEvents(new MiscEventHandler(), this);

        Map<MCVersion, Class<?>> implementations = new HashMap<MCVersion, Class<?>>();
        implementations.put(MCVersion.v1_8, MissileWars1_8.class);
        implementations.put(MCVersion.v1_13, MissileWars1_13.class);
        Instance = this;
        logger = Bukkit.getLogger();
        logger.info("Starting MissileWarsX...");
        logger.info("Getting version compatibility adapter");

        MCVersion version = MCVersion.QueryVersion();
        MCVersion newestVersion = null;
        boolean found = false;
        if(version.getValue() < MCVersion.v1_13.getValue()){
            // legacy
            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() < MCVersion.v1_13.getValue()){
                    found = true;
                    newestVersion = impl.getKey();
                }
            }
        }else{
            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() >= MCVersion.v1_13.getValue()){
                    found = true;
                    newestVersion = impl.getKey();
                }
            }
        }

        if(!found){
            logger.severe("No suitable version adapter found for "+version.toString()+"! MissileWarsX cannot continue executing!");
            throw new RuntimeException();
        }

        logger.info("Found version adapter " + newestVersion + " for version " + version.toString());
        try {
            mwImpl = (MissileWarsImplementation) implementations.get(newestVersion).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        MissileWars = new CoreGame(mwImpl, this);

        logger.info("Registering commands...");
        getCommand("mwmake").setExecutor(new mwmakeCommand());
        getCommand("mwpaste").setExecutor(new mwpasteCommand());
        getCommand("mwlaunch").setExecutor(new mwlaunchCommand());
        getCommand("mwedit").setExecutor(new mweditCommand());
        getCommand("mwreload").setExecutor(new mwreloadCommand());
        getCommand("mwgive").setExecutor(new mwgiveCommand());
        getCommand("reset").setExecutor(new mwresetCommand());
        getCommand("players").setExecutor(new playersCommand());
        getCommand("ready").setExecutor(new readyCommand());
        getCommand("wipe").setExecutor(new mwwipeCommand());
        getCommand("mwitems").setExecutor(new mwitemsCommand());
        getCommand("mwmissiles").setExecutor(new mwmissilesCommand());
        getCommand("start").setExecutor(new mwstartCommand());
        getCommand("spectate").setExecutor(new spectateCommand());
        getCommand("mwteam").setExecutor(new mwteamCommand());
        getCommand("lobby").setExecutor(new lobbyCommand());
        getCommand("ping").setExecutor(new pingCommand());
        getCommand("mode").setExecutor(new modeCommand());

        MissileWars.InitializeGame();

        Bukkit.unloadWorld("world", true);

        LobbyEngine.Fetcher = new SkinFetcher(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        MissileWars.StopGame(true);
    }
}
