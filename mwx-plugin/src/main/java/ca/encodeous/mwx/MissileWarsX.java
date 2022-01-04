package ca.encodeous.mwx;

import ca.encodeous.mwx.mwxcompat1_13.MissileWars1_13;
import ca.encodeous.mwx.mwxcompat1_17.MissileWars1_17;
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
import ca.encodeous.mwx.command.CommandCore;

import java.util.logging.Logger;

public final class MissileWarsX extends JavaPlugin {
    public static MissileWarsX Instance;
    public MissileWarsImplementation mwImpl;
    public CoreGame MissileWars;
    public CommandCore Commands;
    private Logger logger = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        LogManager.getLogManager().getLogger("").setFilter(new ConsoleFilter());
        Bukkit.getServer().getPluginManager().registerEvents(new MiscEventHandler(), this);

        Map<MCVersion, Class<?>> implementations = new HashMap<MCVersion, Class<?>>();
        implementations.put(MCVersion.v1_8, MissileWars1_8.class);
        implementations.put(MCVersion.v1_13, MissileWars1_13.class);
        implementations.put(MCVersion.v1_17, MissileWars1_17.class);
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
            logger.severe("No suitable version adapter found for "+version+"! MissileWarsX cannot continue executing!");
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

        if(version.getValue() < MCVersion.v1_13.getValue()){
            logger.info("You are running MissileWarsX on legacy Minecraft, you will be missing out on many features that only exist on the latest version of Minecraft.");
            logger.info("Startup will be deferred. If startup has not resumed in a few moments, please install MissileWarsX-Compatibility if you have not already.");
        }else{
            ResumeDeferredStartup(this);
            ModernStart();
        }
    }

    public void ResumeDeferredStartup(JavaPlugin resourcePlugin){
        MissileWars = new CoreGame(mwImpl, this, resourcePlugin);
        MissileWars.InitializeGame();
        LobbyEngine.Fetcher = new SkinFetcher(this);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, ()->{
            for(var p : Bukkit.getOnlinePlayers()){
                mwImpl.GetCommandCore().UpdatePlayer(p);
            }
        }, 0, 20);
    }

    private void ModernStart() {
        getLogger().info("Registering commands...");
        mwImpl.GetCommandCore().Initialize();
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        MissileWars.StopGame(true);
        mwImpl.GetCommandCore().Disable();
    }
}
