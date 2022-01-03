package ca.encodeous.mwx.compatibility;

import ca.encodeous.mwx.MissileWarsX;
import ca.encodeous.mwx.commands.*;
import ca.encodeous.mwx.core.utils.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class MissileWarsXCompat extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic

        getLogger().info("Loading Missile Wars Legacy Compatibility...");
        if(MCVersion.QueryVersion().getValue() >= MCVersion.v1_13.getValue()){
            getLogger().severe("MissileWarsX-Compatibility should not be used on modern versions of Minecraft (1.13+)!");
        }else{
            getLogger().info("Resuming deferred startup...");

            MissileWarsX.Instance.ResumeDeferredStartup(this);

            getLogger().info("Registering legacy commands with Spigot API");
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
            getCommand("mwfireball").setExecutor(new mwfireballCommand());
            getCommand("mode").setExecutor(new modeCommand());
        }
    }
}

