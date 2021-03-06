package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.data.TrueSkillPlayer;
import ca.encodeous.mwx.data.TrueSkillTeam;
import ca.encodeous.mwx.mwxstats.MatchParticipation;
import ca.encodeous.mwx.mwxstats.PlayerStats;
import de.gesundkrank.jskills.GameInfo;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.Rating;
import de.gesundkrank.jskills.TrueSkillCalculator;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MissileWarsRankedMatch extends MissileWarsMatch{
    public HashSet<Player> RankedGreen;
    public HashSet<Player> RankedRed;
    public boolean isRedReady;
    public boolean isGreenReady;
    public MissileWarsRankedMatch(Lobby lobby) {
        super(lobby);
        RankedGreen = new HashSet<>();
        RankedRed = new HashSet<>();
    }

    @Override
    protected boolean GreenPadCondition(Player p) {
        if(hasStarted){
            if(!RankedGreen.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Strings.RANKED_LOCKED);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean RedPadCondition(Player p){
        if(hasStarted){
            if(!RankedRed.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Strings.TEAM_FULL);
                return true;
            }
        }
        return false;
    }
    @Override
    protected boolean AutoPadCondition(Player p){
        if(hasStarted){
            if(!RankedRed.contains(p) && !RankedGreen.contains(p)){
                CoreGame.GetImpl().SendActionBar(p, Strings.TEAM_FULL);
                return true;
            }else if(RankedRed.contains(p)){
                AddPlayerToTeam(p, PlayerTeam.Red);
            }else{
                AddPlayerToTeam(p, PlayerTeam.Green);
            }
        }
        return false;
    }

    @Override
    public void CheckGameReadyState() {
        if(hasStarted) return;
        if(Red.size() != 0 && Green.size() != 0){
            if(isGreenReady && isRedReady){
                lobby.SendMessage(Strings.RANKED_LOBBY_TEAM_WARNING);
                startCounter.Start();
            }else{
                lobby.SendMessage(Strings.RANKED_LOBBY_READY);
            }
        }else{
            startCounter.StopCounting();
            if(Red.size() + Green.size() == 1)
                lobby.SendMessage(Strings.RANKED_LOBBY_NOT_ENOUGH_PLAYERS);
            for(Player p : Teams.keySet()){
                p.setLevel(0);
            }
        }
    }

    @Override
    protected void ProcessPlayerAddTeam(Player p, PlayerTeam team){
        if(team == PlayerTeam.Red){
            isRedReady = false;
            for(Player pl : Red){
                pl.sendMessage(Strings.RANKED_PLAYER_JOIN);
            }
        }else if(team == PlayerTeam.Green){
            isGreenReady = false;
            for(Player pl : Green){
                pl.sendMessage(Strings.RANKED_PLAYER_JOIN);
            }
        }
    }

    @Override
    protected void ProcessRemovePlayer(Player p, boolean affectsGame){
        if(affectsGame){
            if(Green.contains(p)){
                isGreenReady = false;
                for(Player pl : Green){
                    pl.sendMessage(Chat.FCL(Strings.RANKED_PLAYER_LEAVE));
                }
            }else{
                isRedReady = false;
                for(Player pl : Red){
                    pl.sendMessage(Chat.FCL(Strings.RANKED_PLAYER_LEAVE));
                }
            }
        }
    }

    @Override
    protected void OnGameStart(){
        RankedGreen.addAll(Green);
        RankedRed.addAll(Red);
    }

    @Override
    public void FinalizeMatch(boolean isDraw) {
        if(!hasStarted){
            return;
        }
        UUID matchId = UUID.randomUUID();
        ArrayList<Player> winTeam = new ArrayList<>();
        ArrayList<Player> loseTeam = new ArrayList<>();
        ArrayList<Player> allTeamPlayers = new ArrayList<>();
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

    public void TeamReady(PlayerTeam team){
        if(team == PlayerTeam.Red){
            lobby.SendMessage(String.format(Strings.RANKED_TEAM_READY, "&cRed"));
            isRedReady = true;
            for(Player p : Red){
                p.sendMessage(Strings.RANKED_TEAM_READY_NOTIF);
            }
        }else{
            lobby.SendMessage(String.format(Strings.RANKED_TEAM_READY, "&aGreen"));
            isGreenReady = true;
            for(Player p : Green){
                p.sendMessage(Strings.RANKED_TEAM_READY_NOTIF);
            }
        }
        CheckGameReadyState();
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
}
