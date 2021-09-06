package ca.encodeous.mwx.mwxcore.gamestate;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsCoreItem;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.MissileWarsEvents;
import ca.encodeous.mwx.mwxcore.missiletrace.TraceEngine;
import ca.encodeous.mwx.mwxcore.utils.*;
import ca.encodeous.mwx.mwxstats.MatchParticipation;
import ca.encodeous.mwx.mwxstats.PlayerStats;
import ca.encodeous.mwx.soundengine.SoundType;
import ca.encodeous.mwx.lobbyengine.Lobby;
import de.gesundkrank.jskills.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MissileWarsMatch {
    // Map info
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
    public boolean isDraw;
    public PlayerTeam winningTeam;
    // ranked
    public boolean isRanked;
    public HashSet<Player> RankedGreen;
    public HashSet<Player> RankedRed;
    public boolean isRedReady;
    public boolean isGreenReady;
    // stats
    public ConcurrentHashMap<UUID, Integer> Deaths;
    public ConcurrentHashMap<UUID, Integer> Kills;

    public MissileWarsMatch(Lobby lobby, boolean ranked) {
        this.lobby = lobby;
        isActivated = true;
        hasStarted = false;
        Green = new HashSet<>();
        None = new HashSet<>();
        Red = new HashSet<>();
        Spectators = new HashSet<>();
        Teams = new ConcurrentHashMap<>();
        Tracer = new TraceEngine();
        EventHandler = new MissileWarsEvents(this);
        startCounter = new Counter(CreateStartGameCountdown(), 20, 15);
        itemCounter = new Counter(new ItemCountdown(this), 20, -1);
        endCounter = new Counter(CreateEndGameCountdown(), 20, CoreGame.Instance.mwConfig.DrawSeconds);
        isRanked = ranked;
        RankedGreen = new HashSet<>();
        RankedRed = new HashSet<>();
        Deaths = new ConcurrentHashMap<>();
        Kills = new ConcurrentHashMap<>();
    }

    public void GreenPad(Player p){
        if(!Map.SeparateJoin) return;
        if(Green.size() >= lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cThis team is full!"));
            return;
        }
        if(isRanked && hasStarted){
            if(!RankedGreen.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cYou cannot join this game because the teams are locked"));
                return;
            }
        }
        RemovePlayer(p);
        AddPlayerToTeam(p, PlayerTeam.Green);
    }
    public void RedPad(Player p){
        if(!Map.SeparateJoin) return;
        if(Red.size() >= lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cThis team is full!"));
            return;
        }
        if(isRanked && hasStarted){
            if(!RankedRed.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cYou cannot join this game because the teams are locked"));
                return;
            }
        }
        RemovePlayer(p);
        AddPlayerToTeam(p, PlayerTeam.Red);
    }
    public void AutoPad(Player p){
        if(Map.SeparateJoin) return;
        if(Red.size() == lobby.teamSize && Green.size() == lobby.teamSize){
            CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cThis game is full!"));
            return;
        }
        if(isRanked && hasStarted){
            if(!RankedRed.contains(p) && !RankedGreen.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&cYou cannot join this game because the teams are locked"));
                return;
            }else if(RankedRed.contains(p)){
                AddPlayerToTeam(p, PlayerTeam.Red);
            }else{
                AddPlayerToTeam(p, PlayerTeam.Green);
            }
        }
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
                int remTime = 15 - count;
                if (remTime == 15 || remTime == 10 || remTime == 3 || remTime == 2) {
                    lobby.SendMessage("&aStarting game in &6" + remTime + "&a seconds!");
                } else if (remTime == 1) {
                    lobby.SendMessage("&aStarting game in &6" + remTime + "&a second!");
                }
                for(Player p : Teams.keySet()){
                    CoreGame.GetImpl().PlaySound(p, SoundType.COUNTDOWN);
                    p.setLevel(remTime);
                    p.setExp(remTime / 15.0f);
                }
            }

            @Override
            public void FinishedCount(Counter counter) {
                lobby.SendMessage("&cStarting now!");
                hasStarted = true;
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
                RankedGreen.addAll(Green);
                RankedRed.addAll(Red);
                itemCounter.Start();
            }
        };
    }

    public Countable CreateEndGameCountdown(){
        return new Countable() {
            @Override
            public void Count(Counter counter, int count) {
                int remTime = CoreGame.Instance.mwConfig.DrawSeconds - count;
                if (remTime == 1) {
                    lobby.SendMessage("&9Resetting game in &6" + remTime + "&a second!");
                }else{
                    lobby.SendMessage("&9Resetting game in &6" + remTime + "&a seconds!");
                }
                for(Player p : Teams.keySet()){
                    CoreGame.GetImpl().PlaySound(p, SoundType.COUNTDOWN);
                    p.setLevel(remTime);
                    p.setExp(remTime / (float)CoreGame.Instance.mwConfig.DrawSeconds);
                }
            }

            @Override
            public void FinishedCount(Counter counter) {
                lobby.SendMessage("&9Clearing map!");
                counter.StopCounting();
                ResetMatchInfo();
                Reset();
            }
        };
    }

    public void PortalBroken(boolean isRed, ArrayList<Player> credits){
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
        for(Player p : credits){
            CoreGame.Stats.Modify(CoreGame.Stats.statsDao, p.getUniqueId(), x->{
                x.PortalsBroken++;
            });
        }
        for(java.util.Map.Entry<Player, PlayerTeam> player : Teams.entrySet()){
            if(player.getValue() == winningTeam)
                CoreGame.GetImpl().PlaySound(player.getKey(), SoundType.WIN);
            CoreGame.GetImpl().PlaySound(player.getKey(), SoundType.GAME_END);
        }
        for(Player p : Teams.keySet()){
            CoreGame.GetImpl().SendActionBar(p, Chat.FCL("&f&lChecking for draw..."));
        }
        endCounter.Start();
    }

    public void EndGame(){
        for(Player p : Teams.keySet()){
            p.setGameMode(GameMode.SPECTATOR);
        }
        for(Player p : lobby.GetPlayers()){
            CoreGame.GetImpl().SendTitle(p, "&9The game has been reset.", "");
        }
        isDraw = true;
        endCounter.Start();
    }

    public void CheckGameReadyState(){
        if(hasStarted) return;
        if(isRanked){
            if(Red.size() != 0 && Green.size() != 0){
                if(isGreenReady && isRedReady){
                    lobby.SendMessage("&aBoth teams are now ready. Once the timer reaches 0, the teams will be locked-in and no changes will be allowed.");
                    startCounter.Start();
                }else{
                    lobby.SendMessage("&9Both teams need to run &6/ready &9to start the game.");
                }
            }else{
                startCounter.StopCounting();
                if(Red.size() + Green.size() == 1)
                    lobby.SendMessage("&9The game needs at least 1 player in each team to start.");
                for(Player p : Teams.keySet()){
                    p.setLevel(0);
                }
            }
        }else{
            if(Red.size() != 0 && Green.size() != 0){
                startCounter.Start();
            }else{
                startCounter.StopCounting();
                if(Red.size() + Green.size() == 1)
                    lobby.SendMessage("&9The game needs at least 1 player in each team to start. To forcefully start a game, run &6/start&9.");
                for(Player p : Teams.keySet()){
                    p.setLevel(0);
                }
            }
        }
    }

    public boolean IsPlayerInTeam(Player p, PlayerTeam team){
        if(Teams.containsKey(p)){
            return Teams.get(p) == team;
        }
        return false;
    }

    public void AddPlayerToTeam(Player p, PlayerTeam team){
        if(IsPlayerInTeam(p, team)) return;
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
            lobby.SendMessage(Chat.FormatPlayerAction(p, "has joined the &cRed &rteam!"));
            if(isRanked){
                isRedReady = false;
                for(Player pl : Red){
                    pl.sendMessage(Chat.FCL("&cA player has joined the team, please run &6/ready &cwhen everyone is ready."));
                }
            }
        }else if(team == PlayerTeam.Green){
            mwGreen.addEntry(p.getName());
            Green.add(p);
            lobby.SendMessage(Chat.FormatPlayerAction(p, "has joined the &aGreen &rteam!"));
            if(isRanked){
                isGreenReady = false;
                for(Player pl : Green){
                    pl.sendMessage(Chat.FCL("&cA player has joined the team, please run &6/ready &cwhen everyone is ready."));
                }
            }
        }else if(team == PlayerTeam.None){
            mwLobby.addEntry(p.getName());
            None.add(p);
            lobby.SendMessage(Chat.FormatPlayerAction(p, "has joined the lobby!"));
            p.setGameMode(GameMode.ADVENTURE);
        }else{
            mwSpectate.addEntry(p.getName());
            Spectators.add(p);
            lobby.SendMessage(Chat.FormatPlayerAction(p, "&9is now spectating!"));
            p.sendMessage(Chat.FCL("&9You are now spectating. Type &6/lobby&9 to return to the lobby."));
            p.setGameMode(GameMode.SPECTATOR);
        }
        CheckGameReadyState();
        if(team != PlayerTeam.Spectator) TeleportPlayer(p, team);
    }

    public void TeamReady(PlayerTeam team){
        lobby.SendMessage("&fThe " + Chat.ResolveTeamColor(team) + team.name() + "&f is now ready.");
        if(team == PlayerTeam.Red){
            isRedReady = true;
            for(Player p : Red){
                p.sendMessage(Chat.FCL("&cYour team is now ready. &6Once the game is started, the teams will be locked and rankings will be calculated when the game ends."));
            }
        }else{
            isGreenReady = true;
            for(Player p : Green){
                p.sendMessage(Chat.FCL("&cYour team is now ready. &6Once the game is started, the teams will be locked and rankings will be calculated when the game ends."));
            }
        }
        CheckGameReadyState();
    }

    public void TeleportPlayer(Player p, PlayerTeam team){
        if((team == PlayerTeam.Green || team == PlayerTeam.Red) && hasStarted){
            CoreGame.GetImpl().PlaySound(p, SoundType.START);
            p.sendMessage(Chat.FCL("&cYou have entered the game, type &6/lobby &cto return to the lobby."));
        }else{
            CoreGame.GetImpl().PlaySound(p, SoundType.TELEPORT);
        }
        Location loc = Utils.GetTeamSpawn(team, this);
        p.setBedSpawnLocation(loc, true);
        p.teleport(loc);
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

    public void RemovePlayer(Player p){
        CleanPlayer(p);
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        boolean affectGame = false;
        if(Green.contains(p) || Red.contains(p)) affectGame = true;
        if(isRanked && affectGame){
            if(Green.contains(p)){
                isGreenReady = false;
                for(Player pl : Green){
                    pl.sendMessage(Chat.FCL("&cA player has left your team, please run &6/ready &cagain when everyone is ready."));
                }
            }else{
                isRedReady = false;
                for(Player pl : Red){
                    pl.sendMessage(Chat.FCL("&cA player has left your team, please run &6/ready &cagain when everyone is ready."));
                }
            }
        }
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

    public static void SendCannotPlaceMessage(Player p){
        p.sendMessage(Chat.FCL("&cYou cannot deploy that there."));
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
                    if(mwItemId.equals(MissileWarsCoreItem.FIREBALL.getValue())) DeployFireball(target, cancel, use, p);
                }
            }
        }
    }

    private void DeployFireball(Block clickedBlock, Ref<Boolean> cancel, Ref<Boolean> use, Player p){
        if(clickedBlock == null) return;
        CoreGame.GetImpl().PlaySound(clickedBlock.getLocation().add(new Vector(0.5, 2, 0.5)), SoundType.FIREBALL);
        Vector loc = clickedBlock.getLocation().toVector().add(new Vector(0.5, 2, 0.5));
        CoreGame.GetImpl().SummonFrozenFireball(loc, clickedBlock.getWorld(), p);
        cancel.val = true;
        use.val = true;
    }

    public void ResetMatchInfo(){
        FinalizeMatch(isDraw);
        if(isRanked){
            RankedGreen = new HashSet<>();
            RankedRed = new HashSet<>();
        }
        isDraw = false;
        itemCounter.StopCounting();
        startCounter.StopCounting();
        Deaths = new ConcurrentHashMap<>();
        Kills = new ConcurrentHashMap<>();
    }

    public static HashMap<UUID, Rating> CalculateTrueSkill(List<PlayerStats> winners, List<PlayerStats> losers){
        GameInfo gi = new GameInfo(1475, 100, 50, 5, 0.05);
        TrueSkillTeam winnersRatings = new TrueSkillTeam();
        TrueSkillTeam losersRatings = new TrueSkillTeam();
        for(PlayerStats stat : winners){
            winnersRatings.put(new TrueSkillPlayer(stat.PlayerId), new Rating(stat.TrueSkill, stat.TrueSkillDev));
        }
        for(PlayerStats stat : losers){
            losersRatings.put(new TrueSkillPlayer(stat.PlayerId), new Rating(stat.TrueSkill, stat.TrueSkillDev));
        }
        Map<IPlayer, Rating> res = TrueSkillCalculator.calculateNewRatings(gi, Arrays.asList(winnersRatings, losersRatings), 1, 2);
        HashMap<UUID, Rating> newStats = new HashMap<>();
        for(Map.Entry<IPlayer, Rating> stat : res.entrySet()){
            newStats.put(((TrueSkillPlayer)stat.getKey()).id, stat.getValue());
        }
        return newStats;
    }

    public void FinalizeMatch(boolean isDraw){
        UUID matchId = UUID.randomUUID();
        ArrayList<Player> winTeam = new ArrayList<>();
        ArrayList<Player> loseTeam = new ArrayList<>();
        ArrayList<Player> allTeamPlayers = new ArrayList<>();
        if(isRanked){
            allTeamPlayers.addAll(RankedRed);
            allTeamPlayers.addAll(RankedGreen);
            if(winningTeam == PlayerTeam.Red){
                winTeam.addAll(RankedRed);
                loseTeam.addAll(RankedGreen);
            }else{
                winTeam.addAll(RankedGreen);
                loseTeam.addAll(RankedRed);
            }
            HashMap<UUID, Rating> newRankings = CalculateTrueSkill(
                    winTeam.stream().map(x->CoreGame.Stats.GetPlayerStats(x)).collect(Collectors.toList()),
                    loseTeam.stream().map(x->CoreGame.Stats.GetPlayerStats(x)).collect(Collectors.toList()));
            for(Player p : allTeamPlayers){
                CoreGame.Stats.Modify(CoreGame.Stats.statsDao, p.getUniqueId(), w->{
                    MatchParticipation x = ConfigureMatchDefaults(matchId, winTeam, p);
                    x.IsRanked = true;
                    x.TrueSkillBefore = w.TrueSkill;
                    x.TrueSkillDevBefore = w.TrueSkillDev;
                    if(isDraw){
                        x.TrueSkillAfter = w.TrueSkill;
                        x.TrueSkillDevAfter = w.TrueSkillDev;
                    }else{
                        x.TrueSkillAfter = newRankings.get(p.getUniqueId()).getMean();
                        x.TrueSkillDevAfter = newRankings.get(p.getUniqueId()).getStandardDeviation();
                        w.TrueSkill = newRankings.get(p.getUniqueId()).getMean();
                        w.TrueSkillDev = newRankings.get(p.getUniqueId()).getStandardDeviation();
                    }
                    try {
                        CoreGame.Stats.matchDao.create(x);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    UpdateStatistics(isDraw, winTeam, p, w);
                    DisplayStatistics(p, x);
                });
            }
            return;
        }
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

    private void UpdateStatistics(boolean isDraw, ArrayList<Player> winTeam, Player p, PlayerStats w) {
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
        w.Kills += Kills.get(p.getUniqueId());
        w.Deaths += Deaths.get(p.getUniqueId());
    }

    private MatchParticipation ConfigureMatchDefaults(UUID matchId, ArrayList<Player> winTeam, Player p) {
        MatchParticipation x = new MatchParticipation();
        x.MatchId = matchId;
        x.HasWon = winTeam.contains(p);
        x.Deaths = Deaths.get(p.getUniqueId());
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
        Wipe(()->{
            lobby.SendMessage("&9The map has been wiped!");
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
        Map.CleanMap(finished);
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
