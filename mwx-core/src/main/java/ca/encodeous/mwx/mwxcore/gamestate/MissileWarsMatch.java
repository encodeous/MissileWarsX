package ca.encodeous.mwx.mwxcore.gamestate;

import ca.encodeous.mwx.configuration.BalanceStrategy;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.utils.Formatter;
import ca.encodeous.mwx.mwxcore.utils.Ref;
import ca.encodeous.mwx.mwxcore.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MissileWarsMatch {
    // Map info
    public MissileWarsMap Map;
    // Teams
    public Team mwGreen, mwRed, mwSpectate, mwLobby;
    // Map State
    public HashSet<Player> Lobby;
    public HashSet<Player> Spectators;
    public HashSet<Player> Green;
    public HashSet<Player> Red;
    public HashMap<Player, PlayerTeam> Teams;
    public boolean isStarting;
    public boolean hasStarted;
    public boolean isActivated;
    public int mwCnt;
    public int CountTaskId = -1;
    public int ItemTaskId = -1;
    public Scoreboard mwScoreboard;
    Random mwRand = new Random();

    public void Initialize(){
        isStarting = false;
        isActivated = true;
        hasStarted = false;
        Green = new HashSet<>();
        Lobby = new HashSet<>();
        Red = new HashSet<>();
        Spectators = new HashSet<>();
        Teams = new HashMap<>();
        CoreGame.Instance.mwImpl.ConfigureScoreboards(this);
    }

    public void RedWin(){
        for(Player p : Teams.keySet()){
            CoreGame.Instance.mwImpl.SendTitle(p, "&6The &cred &6team has won!", "&6Congratulations!");
        }
        EndGame();
    }
    public void GreenWin(){
        for(Player p : Teams.keySet()){
            CoreGame.Instance.mwImpl.SendTitle(p, "&6The &agreen &6team has won!", "&6Congratulations!");
        }
        EndGame();
    }

    public void EndGame(){
        if(hasStarted){
            this.hasStarted = false;
            Bukkit.getScheduler().cancelTask(ItemTaskId);
            for(Player p : Teams.keySet()){
                p.setGameMode(GameMode.SPECTATOR);
            }
            CoreGame.Instance.EndGameCountdown();
        }
    }

    public void GreenPad(Player p){
        if(!Map.SeparateJoin) return;
        RemovePlayer(p);
        AddGreenPlayer(p);
    }
    public void RedPad(Player p){
        if(!Map.SeparateJoin) return;
        RemovePlayer(p);
        AddRedPlayer(p);
    }
    public void AutoPad(Player p){
        if(Map.SeparateJoin) return;
        RemovePlayer(p);
        if(CoreGame.Instance.mwConfig.Strategy == BalanceStrategy.BALANCED_FIXED){
            if(Red.size() == Green.size()){
                if(p.getName().hashCode() % 2 == 0){
                    AddRedPlayer(p);
                }else{
                    AddGreenPlayer(p);
                }
            }else{
                if(Red.size() < Green.size()){
                    AddRedPlayer(p);
                }else{
                    AddGreenPlayer(p);
                }
            }
        }else if(CoreGame.Instance.mwConfig.Strategy == BalanceStrategy.BALANCED_GREEN){
            if(Red.size() == Green.size()){
                AddGreenPlayer(p);
            }else{
                if(Red.size() < Green.size()){
                    AddRedPlayer(p);
                }else{
                    AddGreenPlayer(p);
                }
            }
        }else if(CoreGame.Instance.mwConfig.Strategy == BalanceStrategy.BALANCED_RANDOM){
            if(Red.size() == Green.size()){
                if(mwRand.nextInt() % 2 == 0){
                    AddRedPlayer(p);
                }else{
                    AddGreenPlayer(p);
                }
            }else{
                if(Red.size() < Green.size()){
                    AddRedPlayer(p);
                }else{
                    AddGreenPlayer(p);
                }
            }
        }
    }
    public void GiveItems(){
        ItemTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoreGame.Instance.mwPlugin, new Runnable() {
            @Override
            public void run() {
                if(mwCnt <= 0){
                    while(true){
                        int item = mwRand.nextInt(CoreGame.Instance.mwConfig.Items.size());
                        mwCnt = CoreGame.Instance.mwConfig.ResupplySeconds;
                        MissileWarsItem mwItem = CoreGame.Instance.mwConfig.Items.get(item);
                        if(mwItem.IsExempt) continue;
                        for(Player p : Green){
                            GivePlayerItem(p, CoreGame.Instance.mwConfig.Items.get(item), false);
                        }
                        for(Player p : Red){
                            GivePlayerItem(p, CoreGame.Instance.mwConfig.Items.get(item), true);
                        }
                        break;
                    }
                }
                for(Player p : Green){
                    p.setLevel(mwCnt);
                }
                for(Player p : Red){
                    p.setLevel(mwCnt);
                }
                mwCnt--;
            }
        }, 0, 20);
    }

    public void GivePlayerItem(Player p, MissileWarsItem item, boolean isRed){
        int curCnt = 0;
        for(ItemStack i : p.getInventory()){
            String id = CoreGame.Instance.mwImpl.GetItemId(i);
            if(id.equals(item.MissileWarsItemId)){
                curCnt += i.getAmount();
            }
        }
        if(item.MaxStackSize > curCnt){
            ItemStack citem = CoreGame.Instance.mwImpl.CreateItem(item, isRed);
            citem.setAmount(Math.min(item.StackSize, item.MaxStackSize - curCnt));
            p.getInventory().addItem(citem);
        }else{
            if(isRed){
                CoreGame.Instance.mwImpl.SendActionBar(p, "&6You already have a "+item.RedItemName + "&6.");
            }else{
                CoreGame.Instance.mwImpl.SendActionBar(p, "&6You already have a "+item.GreenItemName + "&6.");
            }
        }
    }

    public void CountdownGame(){
        if(CountTaskId != -1) return;
        mwCnt = 30;
        CountTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoreGame.Instance.mwPlugin, new Runnable() {
            @Override
            public void run() {
                if(!isStarting || hasStarted){
                    Bukkit.getScheduler().cancelTask(CountTaskId);
                    CountTaskId = -1;
                    return;
                }
                if (mwCnt == 60 || mwCnt == 45 || mwCnt == 30 || mwCnt == 15 || mwCnt == 10 || mwCnt == 5 || mwCnt == 4
                        || mwCnt == 3 || mwCnt == 2) {
                    Bukkit.broadcastMessage(Formatter.FCL("&aStarting game in &6" + mwCnt + "&a seconds!"));
                } else if (mwCnt == 1) {
                    Bukkit.broadcastMessage(Formatter.FCL("&aStarting game in &6" + mwCnt + "&a seconds!"));
                } else if (mwCnt == 0) {
                    Bukkit.broadcastMessage(Formatter.FCL("&cStarting now!"));
                    hasStarted = true;
                    isStarting = false;
                    for(Player p : Teams.keySet()){
                        p.setLevel(mwCnt);
                    }
                    GameStarted();
                    Bukkit.getScheduler().cancelTask(CountTaskId);
                }
                for(Player p : Teams.keySet()){
                    p.setLevel(mwCnt);
                }
                mwCnt--;
            }
        }, 0, 20);
    }

    public void GameStarted(){
        for(Player p : Green){
            TeleportGreenPlayer(p);
            p.sendMessage(Formatter.FCL("&cYou have entered a game, type &6/leave §cto return to missile wars lobby."));
        }
        for(Player p : Red){
            TeleportRedPlayer(p);
            p.sendMessage(Formatter.FCL("&cYou have entered a game, type &6/leave §cto return to missile wars lobby."));
        }
        GiveItems();
    }

    public void CheckGameReadyState(){
        if(hasStarted) return;
        if(Red.size() != 0 && Green.size() != 0){
            isStarting = true;
            CountdownGame();
        }else{
            if(isStarting){
                Bukkit.broadcastMessage(Formatter.FCL("&9The game needs at least 2 players to start."));
                isStarting = false;
                for(Player p : Teams.keySet()){
                    p.setLevel(0);
                }
            }
        }
    }

    public void TeleportRedPlayer(Player p){
        Location loc = Utils.LocationFromVec(Map.RedSpawn, Map.MswWorld);
        loc.setYaw(Map.RedYaw);
        loc.setPitch(0);
        p.teleport(loc);
        p.setGameMode(GameMode.SURVIVAL);
    }
    public void TeleportGreenPlayer(Player p){
        Location loc = Utils.LocationFromVec(Map.GreenSpawn, Map.MswWorld);
        loc.setYaw(Map.GreenYaw);
        loc.setPitch(0);
        p.teleport(loc);
        p.setGameMode(GameMode.SURVIVAL);
    }

    public boolean IsPlayerInTeam(Player p, PlayerTeam team){
        if(Teams.containsKey(p)){
            return Teams.get(p) == team;
        }
        return false;
    }

    public void AddRedPlayer(Player p){
        RemovePlayer(p);
        // give player items
        CoreGame.Instance.mwImpl.EquipPlayer(p, true, CoreGame.Instance.GetItemById("gunblade"));
        // add to teams
        mwRed.addEntry(p.getName());
        Red.add(p);
        Teams.put(p, PlayerTeam.Red);
        TeamColorBroadcast(p, p.getName() + " has joined the red team!");
        if(hasStarted){
            TeleportRedPlayer(p);
            return;
        }
        // teleport player
        p.setGameMode(GameMode.ADVENTURE);
        Location loc = Utils.LocationFromVec(Map.RedLobby, Map.MswWorld);
        loc.setYaw(Map.SpawnYaw);
        loc.setPitch(0);
        p.teleport(loc);
        CheckGameReadyState();
    }
    public void AddGreenPlayer(Player p){
        RemovePlayer(p);
        // give player items
        CoreGame.Instance.mwImpl.EquipPlayer(p, false, CoreGame.Instance.GetItemById("gunblade"));
        // add to teams
        mwGreen.addEntry(p.getName());
        Green.add(p);
        Teams.put(p, PlayerTeam.Green);
        TeamColorBroadcast(p, p.getName() + " has joined the green team!");
        if(hasStarted){
            TeleportGreenPlayer(p);
            return;
        }
        // teleport player
        p.setGameMode(GameMode.ADVENTURE);
        Location loc = Utils.LocationFromVec(Map.GreenLobby, Map.MswWorld);
        loc.setYaw(Map.SpawnYaw);
        loc.setPitch(0);
        p.teleport(loc);
        CheckGameReadyState();
    }
    public void AddSpectator(Player p){
        if(IsPlayerInTeam(p, PlayerTeam.Spectator)) return;
        RemovePlayer(p);
        p.sendMessage(Formatter.FCL("&9You are now spectating. Type &6/lobby&9 to return to the lobby."));
        Spectators.add(p);
        mwSpectate.addEntry(p.getName());
        p.setGameMode(GameMode.SPECTATOR);
        Teams.put(p, PlayerTeam.Spectator);
        TeamColorBroadcast(p, p.getName() + " is now spectating!");
    }

    public void TeamColorBroadcast(Player p, String message){
        if(IsPlayerInTeam(p, PlayerTeam.Green)){
            Bukkit.broadcastMessage(Formatter.FCL("&a" + message));
        }else if(IsPlayerInTeam(p, PlayerTeam.Red)){
            Bukkit.broadcastMessage(Formatter.FCL("&c" + message));
        }else if(IsPlayerInTeam(p, PlayerTeam.Spectator)){
            Bukkit.broadcastMessage(Formatter.FCL("&9" + message));
        }else{
            Bukkit.broadcastMessage(Formatter.FCL("&7" + message));
        }
    }

    public void RespawnPlayer(Player p){
        p.setVelocity(new Vector());
        p.setHealth(20);
        //p.res
        if(IsPlayerInTeam(p, PlayerTeam.Spectator)){
            Location loc = Utils.LocationFromVec(Map.Spawn, Map.MswWorld);
            loc.setYaw(Map.SpawnYaw);
            loc.setPitch(0);
            p.teleport(loc);
        }else{
            if(hasStarted){
                if(IsPlayerInTeam(p, PlayerTeam.Green)){
                    TeleportGreenPlayer(p);
                }else if(IsPlayerInTeam(p, PlayerTeam.Red)){
                    TeleportRedPlayer(p);
                }else{
                    AddPlayerToLobby(p);
                }
            }else{
                AddPlayerToLobby(p);
            }
        }
        CoreGame.Instance.mwImpl.MakePlayerTempInvincible(p);
    }

    public void AddPlayerToLobby(Player p){
        if(p.isOnline()){
            RemovePlayer(p);
            p.setScoreboard(mwScoreboard);
            Bukkit.broadcastMessage(Formatter.FCL("&7"+p.getName()+" has joined the lobby!"));
            Lobby.add(p);
            Location loc = Utils.LocationFromVec(Map.Spawn, Map.MswWorld);
            loc.setYaw(Map.SpawnYaw);
            loc.setPitch(0);
            mwLobby.addEntry(p.getName());
            p.teleport(loc);
            p.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void CleanPlayer(Player p){
        p.setLevel(0);
        p.getInventory().clear();
        p.setHealth(20);
    }

    public void RemovePlayer(Player p){
        CleanPlayer(p);
        Teams.remove(p);
        Lobby.remove(p);
        Spectators.remove(p);
        Green.remove(p);
        Red.remove(p);
        mwRed.removeEntry(p.getName());
        mwGreen.removeEntry(p.getName());
        mwSpectate.removeEntry(p.getName());
        mwLobby.removeEntry(p.getName());
        CheckGameReadyState();
    }

    public void MissileWarsItemInteract(Player p, Action action, BlockFace face, Block target, String mwItemId, ItemStack item, boolean isInAir, Ref<Boolean> cancel, Ref<Boolean> use){
        if(hasStarted){
            if(IsPlayerInTeam(p, PlayerTeam.Red)){
                if(CoreGame.Instance.mwMissiles.containsKey(mwItemId) && !isInAir){
                    Missile ms = CoreGame.Instance.mwMissiles.get(mwItemId);
                    CoreGame.Instance.mwImpl.PlaceMissile(ms, target.getLocation().toVector(), target.getWorld(), true, true);
                    cancel.val = true;
                    use.val = true;
                }else{
                    LaunchShield(p, mwItemId, cancel, use, true);
                    if(mwItemId.equals("fireball_spawn")) DeployFireball(target, cancel, use);
                }
            }else if(IsPlayerInTeam(p, PlayerTeam.Green)){
                if(CoreGame.Instance.mwMissiles.containsKey(mwItemId) && !isInAir){
                    Missile ms = CoreGame.Instance.mwMissiles.get(mwItemId);
                    CoreGame.Instance.mwImpl.PlaceMissile(ms, target.getLocation().toVector(), target.getWorld(), false, true);
                    cancel.val = true;
                    use.val = true;
                }else {
                    LaunchShield(p, mwItemId, cancel, use, false);
                    if(mwItemId.equals("fireball_spawn")) DeployFireball(target, cancel, use);
                }
            }
        }
    }

    public HashSet<UUID> AliveSnowballs = new HashSet<>();
    private void DeployFireball(Block clickedBlock, Ref<Boolean> cancel, Ref<Boolean> use){
        if(clickedBlock == null) return;
        Vector loc = clickedBlock.getLocation().toVector().add(new Vector(0.5, 2, 0.5));
        CoreGame.Instance.mwImpl.SummonFrozenFireball(loc, clickedBlock.getWorld());
        cancel.val = true;
        use.val = true;
    }
    private void LaunchShield(Player p, String mwItemId, Ref<Boolean> cancel, Ref<Boolean> use, boolean isRed) {
        if(mwItemId.equals("shield_spawn")){
            final Snowball shield = p.launchProjectile(Snowball.class);
            AliveSnowballs.add(shield.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, new Runnable() {
                public void run() {
                    if(CoreGame.Instance.mwConfig.AllowShieldHit || AliveSnowballs.contains(shield.getUniqueId())){
                        CoreGame.Instance.mwImpl.SpawnShield(shield.getLocation().toVector(), shield.getWorld(), isRed);
                        AliveSnowballs.remove(shield.getUniqueId());
                        shield.remove();
                    }
                }
            }, 20L);
            cancel.val = true;
            use.val = true;
        }
    }

    /**
     * Cleanup the world, and delete the map
     */
    public void Dispose(){
        if(isActivated){
            isActivated = false;
            if(ItemTaskId != -1){
                Bukkit.getScheduler().cancelTask(ItemTaskId);
                ItemTaskId = -1;
            }
            if(CountTaskId != -1){
                Bukkit.getScheduler().cancelTask(CountTaskId);
                CountTaskId = -1;
            }
            File worldFolder = Map.MswWorld.getWorldFolder();
            boolean firstTry = Bukkit.unloadWorld(Map.MswWorld.getName(), false);
            if(!firstTry){
                for(Player p : Map.MswWorld.getPlayers()){
                    p.kickPlayer("Resetting Map");
                }
                Bukkit.unloadWorld(Map.MswWorld.getName(), false);
            }
            try {
                FileUtils.deleteDirectory(worldFolder);
            } catch (IOException e) {
                try {
                    FileUtils.forceDeleteOnExit(worldFolder);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            Map = null;
        }
    }
}
