package ca.encodeous.mwx.compatibility;

import ca.encodeous.mwx.MissileWarsX;
import ca.encodeous.mwx.command.commands.*;
import ca.encodeous.mwx.core.utils.MCVersion;
import org.bukkit.plugin.java.JavaPlugin;

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
            getCommand("mwmake").setExecutor(new mwmake());
            getCommand("mwpaste").setExecutor(new mwpaste());
            getCommand("mwlaunch").setExecutor(new mwlaunch());
            getCommand("mwedit").setExecutor(new mwedit());
            getCommand("mwreload").setExecutor(new mwreload());
            getCommand("mwgive").setExecutor(new mwgive());
            getCommand("reset").setExecutor(new reset());
            getCommand("players").setExecutor(new players());
            getCommand("ready").setExecutor(new ready());
            getCommand("wipe").setExecutor(new wipe());
            getCommand("mwitems").setExecutor(new mwitems());
            getCommand("mwmissiles").setExecutor(new mwmissiles());
            getCommand("start").setExecutor(new start());
            getCommand("spectate").setExecutor(new spectate());
            getCommand("mwteam").setExecutor(new mwteam());
            getCommand("lobby").setExecutor(new lobby());
            getCommand("ping").setExecutor(new ping());
            getCommand("mwfireball").setExecutor(new mwfireball());
            getCommand("gms").setExecutor(new gms());
            getCommand("gmc").setExecutor(new gmc());
        }
    }
}

