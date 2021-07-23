package ca.encodeous.mwx.mwxcore;

import ca.encodeous.mwx.configuration.BalanceStrategy;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsConfiguration;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Bounds;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.ResourceLoader;
import ca.encodeous.mwx.mwxcore.world.MissileBlock;
import ca.encodeous.mwx.mwxcore.world.MissileSchematic;
import ca.encodeous.mwx.mwxcore.world.PistonData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

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

    public JavaPlugin mwPlugin;

    // Missile Wars
    public MissileWarsImplementation mwImpl;
    public MissileWarsMatch mwMatch = null;
    public MissileWarsMatch newMatch = null;
    public HashMap<String, Missile> mwMissiles = null;
    public World mwAuto = null, mwManual = null;
    public int mwWorldCount = 0;

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
        mwMatch.Dispose();
        InitializeGame();
    }

    public void InitializeGame(){
        if(Instance == null){
            Instance = this;
            mwImpl.RegisterEvents(new MissileWarsEvents(), mwPlugin);
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

        mwMatch = CreateMatch();
    }

    int endGameTask = -1;
    public void EndGameCountdown(){
        if(endGameTask != -1){
            return;
        }
        Bukkit.broadcastMessage(Formatter.FCL("&9Resetting game in &65&a seconds!"));
        endGameTask = Bukkit.getScheduler().scheduleSyncDelayedTask(mwPlugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(Formatter.FCL("&9Resetting game...!"));
                EndMatch();
            }
        }, 5 * 20);
    }

    public void PrepareEndMatch(){
        if(newMatch != null){
            EndMatch();
        }else{
            newMatch = CreateMatch();
        }
    }

    public void EndMatch(){
        if(endGameTask != -1){
            Bukkit.getScheduler().cancelTask(endGameTask);
            endGameTask = -1;
        }
        if(mwMatch.isStarting){
            mwMatch.isStarting = false;
            return;
        }
        if(newMatch == null){
            PrepareEndMatch();
        }
        HashSet<Player> allPlayers = new HashSet<>();
        for(Player p : mwMatch.Lobby) allPlayers.add(p);
        for(Player p : mwMatch.Red) allPlayers.add(p);
        for(Player p : mwMatch.Green) allPlayers.add(p);
        for(Player p : mwMatch.Spectators) allPlayers.add(p);
        for(Player p : allPlayers){
            newMatch.AddPlayerToTeam(p, PlayerTeam.None);
        }
        mwMatch.Dispose();
        mwMatch = newMatch;
        newMatch = null;
    }

    public MissileWarsItem GetItemById(String id){
        for(MissileWarsItem i : mwConfig.Items){
            if(i.MissileWarsItemId.equals(id)) return i;
        }
        return null;
    }

    public MissileWarsMatch CreateMatch(){
        MissileWarsMatch match = new MissileWarsMatch();
        if(mwConfig.Strategy == BalanceStrategy.PLAYERS_CHOOSE){
            match.Map = mwImpl.CreateManualJoinMap("mwx_match_"+mwWorldCount);
        }else{
            match.Map = mwImpl.CreateAutoJoinMap("mwx_match_"+mwWorldCount);
        }
        mwWorldCount++;
        match.Initialize();
        return match;
    }

    public void StopGame(boolean save){
        if(mwMatch != null){
            mwMatch.Dispose();
        }
        if(save){
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ResourceLoader.SaveMissiles(mwPlugin, mwMissiles);
        }
    }
}
