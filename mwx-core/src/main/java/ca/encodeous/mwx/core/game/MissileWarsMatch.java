package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.core.settings.BooleanSetting;
import ca.encodeous.mwx.core.settings.SettingsManager;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.engines.trace.TraceEngine;
import ca.encodeous.mwx.core.utils.*;
import ca.encodeous.mwx.mwxstats.MatchParticipation;
import ca.encodeous.mwx.mwxstats.PlayerStats;
import ca.encodeous.mwx.data.SoundType;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.structure.StructureUtils;
import de.gesundkrank.jskills.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissileWarsMatch {
    // Map info
    public SettingsManager settingsManager;
    public MissileWarsMap Map;
    // Teams
    public static Team mwGreen, mwRed, mwSpectate, mwLobby;
    // Map State
    public HashSet<Player> None;
    public HashSet<Player> Spectators;
    public HashSet<Player> Green;
    public HashSet<Player> Red;
    public ConcurrentHashMap<Player, PlayerTeam> Teams;
    public boolean hasStarted;
    public boolean isActivated;
    public Counter startCounter,
            endCounter,
            itemCounter;
    public MissileWarsEvents EventHandler;
    public TraceEngine Tracer;
    public Lobby lobby;
    Random mwRand = new Random();
    public boolean isDraw = false, isRedPortalBroken = false, isGreenPortalBroken = false;
    public PlayerTeam winningTeam;
    // stats
    public ConcurrentHashMap<UUID, Integer> Deaths;
    public ConcurrentHashMap<UUID, Integer> Kills;

    public MissileWarsMatch(Lobby lobby) {
        this.lobby = lobby;
        isActivated = true;
        hasStarted = false;
        settingsManager = new MissileWarsMatchSettingsManager();
        Green = new HashSet<>();
        None = new HashSet<>();
        Red = new HashSet<>();
        Spectators = new HashSet<>();
        Teams = new ConcurrentHashMap<>();
        Tracer = new TraceEngine();
        EventHandler = new MissileWarsEvents(this);
        startCounter = new Counter(CreateStartGameCountdown(), 10, 60); // 30 second counter
        itemCounter = new Counter(new ItemCountdown(this), 20, -1);
        endCounter = new Counter(CreateEndGameCountdown(), 20, CoreGame.Instance.mwConfig.DrawSeconds);
        Deaths = new ConcurrentHashMap<>();
        Kills = new ConcurrentHashMap<>();
    }

    protected boolean GreenPadCondition(Player p){ return false; }
    protected boolean RedPadCondition(Player p){ return false; }
    protected boolean AutoPadCondition(Player p){ return false; }
    protected void OnGameStart(){ }

    public boolean AllowPlayerInteractProtectedRegion(Player p){
        if(p.getGameMode() != GameMode.CREATIVE) return false;
        else return true;
    }

    public void UpdatePlayers() {
        Iterator<Player> it = Teams.keys().asIterator();
        while(it.hasNext()) {
            RefreshPlayerSettings(it.next());
        }
    }

    public void RefreshPlayerSettings(Player player) {
        boolean noHitDelay = settingsManager.getBooleanSetting("NoHitDelay").getValue();
        player.setMaximumNoDamageTicks(noHitDelay ? 0 : 20);
        if(noHitDelay) player.setLastDamage(Integer.MAX_VALUE);
        player.recalculatePermissions();
    }

    public void GreenPad(Player p){
        if(!Map.SeparateJoin) return;
        if(Green.size() >= lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Strings.TEAM_FULL);
            return;
        }

        if(GreenPadCondition(p)) return;

        RemovePlayer(p);
        AddPlayerToTeam(p, PlayerTeam.Green);
    }
    public void RedPad(Player p){
        if(!Map.SeparateJoin) return;
        if(Red.size() >= lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Strings.TEAM_FULL);
            return;
        }

        if(RedPadCondition(p)) return;

        RemovePlayer(p);
        AddPlayerToTeam(p, PlayerTeam.Red);
    }
    public void AutoPad(Player p){
        if(Map.SeparateJoin) return;
        if(Red.size() == lobby.teamSize && Green.size() == lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Strings.GAME_FULL);
            return;
        }
        if(AutoPadCondition(p)) return;
        RemovePlayer(p);
        PlayerTeam team;
        if(Red.size() == Green.size()){
            if(mwRand.nextInt() % 2 == 0) team = PlayerTeam.Red;
            else team = PlayerTeam.Green;
        }else{
            if(Red.size() < Green.size()) team = PlayerTeam.Red;
            else team = PlayerTeam.Green;
        }
        AddPlayerToTeam(p, team);
    }

    public Countable CreateStartGameCountdown(){
        return new Countable() {
            @Override
            public void Count(Counter counter, int count) {
                int remTime = 60 - count;
                if (remTime == 60 || remTime == 40 || remTime == 20 || remTime == 10 || remTime == 8 || remTime == 6 || remTime == 4) { // Multiplied by 2 to give 0.5s interval
                    lobby.SendMessage(String.format(Strings.STARTING_GAME_PLURAL, remTime / 2));
                } else if (remTime == 2) {
                    lobby.SendMessage(String.format(Strings.STARTING_GAME, remTime / 2));
                }
                for(Player p : Teams.keySet()){
                    if(remTime % 2 == 0) CoreGame.GetImpl().PlaySound(p, SoundType.COUNTDOWN);
                    p.setLevel(remTime / 2);
                    p.setExp(remTime / 60.0f);
                }
            }

            @Override
            public void FinishedCount(Counter counter) {
                BeginGame();
            }
        };
    }

    public void BeginGame(){
        lobby.SendMessage(Strings.STARTING_NOW);
        hasStarted = true;
        startCounter.StopCounting();
        for(Player p : Teams.keySet()){
            p.setLevel(0);
        }
        for(Player p : Green){
            TeleportPlayer(p, PlayerTeam.Green);
            p.setGameMode(GameMode.SURVIVAL);
        }
        for(Player p : Red){
            TeleportPlayer(p, PlayerTeam.Red);
            p.setGameMode(GameMode.SURVIVAL);
        }
        OnGameStart();
        UpdatePlayers();
        itemCounter.Start();
    }

    public Countable CreateEndGameCountdown(){
        return new Countable() {
            @Override
            public void Count(Counter counter, int count) {
                int remTime = CoreGame.Instance.mwConfig.DrawSeconds - count;
                if (remTime == 1) {
                    lobby.SendMessage(String.format(Strings.RESETTING_GAME, remTime));
                }else{
                    lobby.SendMessage(String.format(Strings.RESETTING_GAME_PLURAL, remTime));
                }
                for(Player p : Teams.keySet()){
                    CoreGame.GetImpl().PlaySound(p, SoundType.COUNTDOWN);
                    p.setLevel(remTime);
                    p.setExp(remTime / (float)CoreGame.Instance.mwConfig.DrawSeconds);
                }
            }

            @Override
            public void FinishedCount(Counter counter) {
                lobby.SendMessage(Strings.CLEARING_MAP);
                counter.StopCounting();
                ResetMatchInfo();
                Reset();
            }
        };
    }

    protected void UpdatePortalBrokenStatistics(ArrayList<Player> credits){
        for(Player p : credits){
            CoreGame.Stats.Modify(CoreGame.Stats.statsDao, p.getUniqueId(), x->{
                x.PortalsBroken++;
            });
        }
    }
    public void PortalBroken(boolean isRed, ArrayList<Player> credits){
        if(isDraw) return;
        if(isRed && isRedPortalBroken) return;
        if(!isRed && isGreenPortalBroken) return;
        isRedPortalBroken = isRedPortalBroken || isRed;
        isGreenPortalBroken = isGreenPortalBroken || !isRed;
        winningTeam = isRed? PlayerTeam.Green : PlayerTeam.Red;
        PlayerTeam lose = isRed? PlayerTeam.Red : PlayerTeam.Green;
        if(endCounter.isRunning()){
            Chat.TeamDraw(credits, lobby, lose);
            isDraw = true;
        }else{
            Chat.TeamWin(credits, lobby, winningTeam, lose);
            for(Player p : Teams.keySet()){
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
        UpdatePortalBrokenStatistics(credits);
        for(java.util.Map.Entry<Player, PlayerTeam> player : Teams.entrySet()){
            if(player.getValue() == winningTeam)
                CoreGame.GetImpl().PlaySound(player.getKey(), SoundType.WIN);
            CoreGame.GetImpl().PlaySound(player.getKey(), SoundType.GAME_END);
        }
        if(!isDraw){
            for(Player p : Teams.keySet()){
                CoreGame.GetImpl().SendActionBar(p, Strings.DRAW_CHECK);
            }
        }
        endCounter.Start();
    }

    public void EndGame(){
        for(Player p : Teams.keySet()){
            p.setGameMode(GameMode.SPECTATOR);
        }
        for(Player p : lobby.GetPlayers()){
            CoreGame.GetImpl().SendTitle(p, Strings.GAME_RESET, "");
            p.recalculatePermissions();
        }
        isDraw = true;
        endCounter.Start();
    }

    public void CheckGameReadyState(){
        if(Red.size() != 0 && Green.size() != 0){
            startCounter.Start();
        }else{
            startCounter.StopCounting();
            if(Red.size() + Green.size() == 1)
                lobby.SendMessage(Strings.LOBBY_NOT_ENOUGH_PLAYERS);
            for(Player p : Teams.keySet()){
                p.setLevel(0);
            }
        }
    }

    public boolean IsPlayerInTeam(Player p, PlayerTeam team){
        if(Teams.containsKey(p)){
            return Teams.get(p) == team;
        }
        return false;
    }

    protected void ProcessPlayerAddTeam(Player p, PlayerTeam team){ };

    public void AddPlayerToTeam(Player p, PlayerTeam team){
        if(IsPlayerInTeam(p, team)) return;
        boolean affectGame = false;
        Kills.putIfAbsent(p.getUniqueId(), 0);
        Deaths.putIfAbsent(p.getUniqueId(), 0);
        RemovePlayer(p);
        if(team == PlayerTeam.Green || team == PlayerTeam.Red){
            CoreGame.GetImpl().EquipPlayer(p, team == PlayerTeam.Red);
            if(hasStarted) p.setGameMode(GameMode.SURVIVAL);
            else p.setGameMode(GameMode.ADVENTURE);
        }
        Teams.put(p, team);
        SetPlayerDisplayName(team, p);
        if(team == PlayerTeam.Red){
            mwRed.addEntry(p.getName());
            Red.add(p);
            lobby.SendMessage(String.format(Strings.PLAYER_JOIN_TEAM, p.getDisplayName(), "&cRed"));
            ProcessPlayerAddTeam(p, team);
            affectGame = true;
        }else if(team == PlayerTeam.Green){
            mwGreen.addEntry(p.getName());
            Green.add(p);
            lobby.SendMessage(String.format(Strings.PLAYER_JOIN_TEAM, p.getDisplayName(), "&aGreen"));
            ProcessPlayerAddTeam(p, team);
            affectGame = true;
        }else if(team == PlayerTeam.None){
            mwLobby.addEntry(p.getName());
            None.add(p);
            p.setGameMode(GameMode.ADVENTURE);
        }else{
            mwSpectate.addEntry(p.getName());
            Spectators.add(p);
            lobby.SendMessage(String.format(Strings.PLAYER_SPECTATE, p.getDisplayName()));
            p.sendMessage(Strings.PLAYER_SPECTATE_NOTIF);
            p.setGameMode(GameMode.SPECTATOR);
        }
        if(affectGame && !hasStarted) CheckGameReadyState();
        if(team != PlayerTeam.Spectator) TeleportPlayer(p, team);
        RefreshPlayerSettings(p);
        p.recalculatePermissions();
    }


    public void TeleportPlayer(Player p, PlayerTeam team){
        Location loc = Utils.GetTeamSpawn(team, this);
        p.setBedSpawnLocation(loc, true);
        p.teleport(loc);
        if((team == PlayerTeam.Green || team == PlayerTeam.Red) && hasStarted){
            CoreGame.GetImpl().PlaySound(p, SoundType.START);
            p.sendMessage(Strings.ENTER_GAME);
        }else{
            CoreGame.GetImpl().PlaySound(p, SoundType.TELEPORT);
        }
    }

    public void SetPlayerDisplayName(PlayerTeam team, Player p){
        p.setDisplayName(Chat.ResolveTeamColor(team) + Chat.FCL(p.getName() + "&r"));
    }

    public void CleanPlayer(Player p){
        for(PotionEffect effects : p.getActivePotionEffects()){
            p.removePotionEffect(effects.getType());
        }
        p.setExp(0);
        p.setLevel(0);
        p.setFireTicks(0);
        p.setFoodLevel(20);
        p.getInventory().clear();
        p.setHealth(20);
    }

    protected void ProcessRemovePlayer(Player p, boolean affectsGame) {};

    public void RemovePlayer(Player p){
        CleanPlayer(p);
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        PlayerTeam team = Teams.get(p);
        if(team == PlayerTeam.Red){
            lobby.SendMessage(String.format(Strings.PLAYER_LEAVE_TEAM, p.getDisplayName(), "&cRed"));
        }else if(team == PlayerTeam.Green){
            lobby.SendMessage(String.format(Strings.PLAYER_LEAVE_TEAM, p.getDisplayName(), "&aGreen"));
        }else if(team == PlayerTeam.Spectator){
            lobby.SendMessage(String.format(Strings.PLAYER_STOP_SPECTATE, p.getDisplayName()));
        }
        boolean affectGame = false;
        if(Green.contains(p) || Red.contains(p)) affectGame = true;
        ProcessRemovePlayer(p, affectGame);
        Teams.remove(p);
        None.remove(p);
        Spectators.remove(p);
        Green.remove(p);
        Red.remove(p);
        try{
            mwRed.removeEntry(p.getName());
            mwGreen.removeEntry(p.getName());
            mwSpectate.removeEntry(p.getName());
            mwLobby.removeEntry(p.getName());
        }catch(IllegalStateException e){

        }
        if(affectGame && !hasStarted) CheckGameReadyState();
    }

    public static void SendCannotPlaceMessage(CommandSender p){
        p.sendMessage(Strings.CANNOT_DEPLOY);
    }

    public void MissileWarsItemInteract(Player p, Block target, String mwItemId, ItemStack item, boolean isInAir, Ref<Boolean> cancel, Ref<Boolean> use){
        if(hasStarted){
            if(IsPlayerInTeam(p, PlayerTeam.Red) || IsPlayerInTeam(p, PlayerTeam.Green)){
                if(CoreGame.Instance.mwMissiles.containsKey(mwItemId) && !isInAir){
                    Missile ms = CoreGame.Instance.mwMissiles.get(mwItemId);
                    boolean result = CoreGame.GetImpl().GetStructureManager().PlaceMissile(ms, target.getLocation().toVector(),
                            target.getWorld(), IsPlayerInTeam(p, PlayerTeam.Red), true, p);
                    if(result){
                        use.val = true;
                    }else{
                        use.val = false;
                        SendCannotPlaceMessage(p);
                    }
                    cancel.val = true;
                }else{
                    StructureUtils.LaunchShield(p, mwItemId, cancel, use, IsPlayerInTeam(p, PlayerTeam.Red), this);
                    if(mwItemId.equals(MissileWarsCoreItem.FIREBALL.getValue())){
                        DeployFireball(target, cancel, use, p);
                        cancel.val = true;
                    }
                }
            }
        }
    }

    public void DeployFireball(Block clickedBlock, Ref<Boolean> cancel, Ref<Boolean> use, Player p){
        if(clickedBlock == null) return;
        Location nLoc = clickedBlock.getLocation().add(new Vector(0.5, 2, 0.5));
        if(!nLoc.getBlock().getType().isSolid()){
            CoreGame.GetImpl().PlaySound(clickedBlock.getLocation().add(new Vector(0.5, 2, 0.5)), SoundType.FIREBALL);
            Vector loc = clickedBlock.getLocation().toVector().add(new Vector(0.5, 2, 0.5));
            CoreGame.GetImpl().SummonFrozenFireball(loc, clickedBlock.getWorld(), p);
            use.val = true;
        }
    }

    protected void ProcessResetMatchInfo(){};

    public void ResetMatchInfo(){
        FinalizeMatch(isDraw);
        ProcessResetMatchInfo();
        isDraw = false;
        itemCounter.StopCounting();
        startCounter.StopCounting();
        Deaths = new ConcurrentHashMap<>();
        Kills = new ConcurrentHashMap<>();
        isRedPortalBroken = false;
        isGreenPortalBroken = false;
    }

    public void FinalizeMatch(boolean isDraw){
        UUID matchId = UUID.randomUUID();
        ArrayList<Player> winTeam = new ArrayList<>();
        ArrayList<Player> allTeamPlayers = new ArrayList<>();
        allTeamPlayers.addAll(Red);
        allTeamPlayers.addAll(Green);
        if(winningTeam == PlayerTeam.Red){
            winTeam.addAll(Red);
        }else{
            winTeam.addAll(Green);
        }
        // update stats
        for(Player p : allTeamPlayers){
            try {
                MatchParticipation info = ConfigureMatchDefaults(matchId, winTeam, p);
                DisplayStatistics(p, info);
                CoreGame.Stats.matchDao.create(info);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            CoreGame.Stats.Modify(CoreGame.Stats.statsDao, p.getUniqueId(), w->{
                UpdateStatistics(isDraw, winTeam, p, w);
            });
        }
    }

    protected void UpdateStatistics(boolean isDraw, ArrayList<Player> winTeam, Player p, PlayerStats w) {
        if(isDraw){
            w.Draws++;
        }else if(winTeam.contains(p)){
            w.Wins++;
            w.Streak++;
            w.MaxStreak = Math.max(w.MaxStreak, w.Streak);
        }else{
            w.Streak = 0;
            w.Losses++;
        }
        w.Kills += Kills.getOrDefault(p.getUniqueId(), 0);
        w.Deaths += Deaths.getOrDefault(p.getUniqueId(), 0);
    }

    protected MatchParticipation ConfigureMatchDefaults(UUID matchId, ArrayList<Player> winTeam, Player p) {
        MatchParticipation x = new MatchParticipation();
        x.MatchId = matchId;
        x.HasWon = winTeam.contains(p);
        x.Deaths = Deaths.getOrDefault(p.getUniqueId(), 0);
        x.PlayerId = p.getUniqueId();
        x.IsRanked = false;
        x.Kills = Kills.get(p.getUniqueId());
        x.EndTime = Date.from(Instant.now());
        return x;
    }

    public void DisplayStatistics(Player p, MatchParticipation match){
        if(match.IsRanked){
            Rating original = new Rating(match.TrueSkillBefore, match.TrueSkillDevBefore);
            Rating result = new Rating(match.TrueSkillAfter, match.TrueSkillDevAfter);
            p.sendMessage(Chat.FCL(
                    "&f&lRANKED MATCH RESULTS\n" +
                    "&7Trueskill Change: &6" + Chat.F(original.getConservativeRating()) + "&7, &6" + Chat.F(original.getStandardDeviation())
                            + " &7-> &6" + Chat.F(result.getConservativeRating()) + "&7, &6" + Chat.F(result.getStandardDeviation())));
        }
        p.sendMessage(Chat.FCL("&f&lMATCH STATISTICS\n&7In this match you have died &6"
                + match.Deaths + " &7times and killed &6" + match.Kills + " &7players."));
    }

    public void Reset(){
        itemCounter.StopCounting();
        startCounter.StopCounting();
        EventHandler = new MissileWarsEvents(this);
        settingsManager = new MissileWarsMatchSettingsManager();
        Wipe(()->{
            lobby.SendMessage(Strings.MAP_WIPED);
            ArrayList<Player> players = new ArrayList<>(Teams.keySet());
            for(Player p : players){
                RemovePlayer(p);
                AddPlayerToTeam(p, PlayerTeam.None);
            }
            hasStarted = false;
        });
    }

    public void Wipe(Runnable finished){
        Tracer = new TraceEngine();
        lobby.SendMessage(Strings.REMOVING_ENTITIES);
        CoreGame.GetImpl().ClearEntities(Map.MswWorld);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoreGame.Instance.mwPlugin, ()->{
            Map.CleanMap(finished);
        }, 1);
    }

    /**
     * Cleanup the world, and delete the map
     */
    public void Dispose(){
        if(isActivated){
            isActivated = false;
            itemCounter.StopCounting();
            startCounter.StopCounting();
            endCounter.StopCounting();
            Map.Delete();
        }
    }
}
