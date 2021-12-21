package ca.encodeous.mwx.mwxcore.gamestate;

import ca.encodeous.mwx.lobbyengine.Lobby;
import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.lang.Strings;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import ca.encodeous.mwx.mwxstats.MatchParticipation;
import de.gesundkrank.jskills.Rating;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class MissileWarsPracticeMatch extends MissileWarsMatch{
    public MissileWarsPracticeMatch(Lobby lobby) {
        super(lobby);
    }

    public boolean AllowPlayerInteractProtectedRegion(Player p){
        if(!p.isOp()) return false;
        else return true;
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
    public void FinalizeMatch(boolean isDraw) {
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
}
