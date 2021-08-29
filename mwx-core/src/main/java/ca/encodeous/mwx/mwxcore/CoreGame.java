package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsConfiguration;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.*;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import ca.encodeous.mwx.mwxcore.world.PistonData;
import lobbyengine.Lobby;
import lobbyengine.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import pl.kacperduras.protocoltab.manager.TabManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class CoreGame {
    static{
        ConfigurationSerialization.registerClass(MissileWarsConfiguration.class, "missilewars-settings");
        ConfigurationSerialization.registerClass(MissileWarsItem.class, "item");
        ConfigurationSerialization.registerClass(Missile.class, "missile");
        ConfigurationSerialization.registerClass(MissileBlock.class, "mblock");
        ConfigurationSerialization.registerClass(MissileSchematic.class, "mschem");
        ConfigurationSerialization.registerClass(PistonData.class, "pdata");
        ConfigurationSerialization.registerClass(Bounds.class, "bounds");
    }
    public static CoreGame Instance = null;

    public CoreGame(MissileWarsImplementation implementation, JavaPlugin plugin){
        mwImpl = implementation;
        mwPlugin = plugin;
    }
    public static MissileWarsImplementation GetImpl(){
        return Instance.mwImpl;
    }

    public static StructureInterface GetStructureManager(){
        return Instance.mwImpl.GetStructureManager();
    }

    public JavaPlugin mwPlugin;

    // Missile Wars
    private MissileWarsImplementation mwImpl;
    public HashMap<String, Missile> mwMissiles = null;
    public TabManager tabManager = null;
    public World mwAuto = null, mwManual = null;

    // Configuration
    public MissileWarsConfiguration mwConfig;
    FileConfiguration config = new YamlConfiguration();
    File configFile = null;

    public void LoadConfig(){
        // load configuration
        File configDir = mwPlugin.getDataFolder();
        configFile = new File(configDir, "config.yml");
        if(configFile.exists()){
            config = YamlConfiguration.loadConfiguration(configFile);
            mwConfig = (MissileWarsConfiguration) config.get("data");
        }else{
            configFile.getParentFile().mkdirs();
            mwConfig = new MissileWarsConfiguration();
            mwConfig.Items = mwImpl.CreateDefaultItems();
            config.set("data", mwConfig);
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Reload(){
        Bukkit.unloadWorld("mwx_template_auto", true);
        Bukkit.unloadWorld("mwx_template_manual", true);
        LobbyEngine.Shutdown();
        InitializeGame();
    }

    public void InitializeGame(){
        if(Instance == null){
            Instance = this;
            mwImpl.RegisterEvents(mwPlugin);
        }
        LoadConfig();
        // load worlds
        mwPlugin.getLogger().info("Loading template worlds...");
        try {
            ResourceLoader.LoadWorldFiles(mwPlugin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mwAuto = new WorldCreator("mwx_template_auto").environment(World.Environment.NORMAL).createWorld();
        mwManual = new WorldCreator("mwx_template_manual").environment(World.Environment.NORMAL).createWorld();

        mwAuto.setAutoSave(true);
        mwManual.setAutoSave(true);

        if(!new File(mwPlugin.getDataFolder(), "missiles").exists()){
            mwPlugin.getLogger().info("Loading default missiles...");
            try {
                ResourceLoader.UnzipMissiles(mwPlugin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mwPlugin.getLogger().info("Loading missiles...");
        mwMissiles = ResourceLoader.LoadMissiles(mwPlugin);
        mwPlugin.getLogger().info("MissileWarsX fully loaded!");

        mwPlugin.getLogger().info("Caching temporary lobbies...");
        LobbyEngine.EnsureCache(false, CoreGame.Instance.mwConfig.TempCache);
        mwPlugin.getLogger().info("Finished loading lobby cache!");

        mwPlugin.getLogger().info("Creating default lobby...");
        LobbyEngine.CreateLobby("default", 6, false, true);

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(mwPlugin, TPSMon.Instance, 0, 20);
        scheduler.runTaskTimer(mwPlugin, RealTPS.Instance, 0, 20);
        tabManager = new TabManager(mwPlugin);
    }

    public MissileWarsItem GetItemById(String id){
        for(MissileWarsItem i : mwConfig.Items){
            if(i.MissileWarsItemId.equals(id)) return i;
        }
        return null;
    }

    public void StopGame(boolean save){
        LobbyEngine.Shutdown();
        if(save){
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ResourceLoader.SaveMissiles(mwPlugin, mwMissiles);
        }
    }

    public void PlayerJoined(Player p){
        LobbyEngine.GetLobby("default").AddPlayer(p);
    }
    public void PlayerLeft(Player p){
        MissileWarsMatch match = LobbyEngine.FromPlayer(p);
        if(match != null){
            match.lobby.RemovePlayer(p);
        }
    }
}
