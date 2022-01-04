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
            getCommand("mwmake").setExecutor(new MakeCommand());
            getCommand("mwpaste").setExecutor(new PasteCommand());
            getCommand("mwlaunch").setExecutor(new LaunchCommand());
            getCommand("mwedit").setExecutor(new LobbyEditCommand());
            getCommand("mwreload").setExecutor(new ReloadCommand());
            getCommand("mwgive").setExecutor(new GiveCommand());
            getCommand("reset").setExecutor(new ResetCommand());
            getCommand("players").setExecutor(new PlayersCommand());
            getCommand("ready").setExecutor(new ReadyCommand());
            getCommand("wipe").setExecutor(new WipeCommand());
            getCommand("mwitems").setExecutor(new ListItemsCommand());
            getCommand("mwmissiles").setExecutor(new ListMissilesCommand());
            getCommand("start").setExecutor(new StartCommand());
            getCommand("spectate").setExecutor(new SpectateCommand());
            getCommand("mwteam").setExecutor(new TeamCommand());
            getCommand("lobby").setExecutor(new LobbyCommands());
            getCommand("ping").setExecutor(new PingCommand());
            getCommand("mwfireball").setExecutor(new FireballCommand());
            getCommand("mode").setExecutor(new GamemodeCommand());
        }
    }
}

