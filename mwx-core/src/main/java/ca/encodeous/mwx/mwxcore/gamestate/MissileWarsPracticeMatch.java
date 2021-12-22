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
    protected void ProcessPlayerAddTeam(Player p, PlayerTeam team) {
        p.sendMessage(Strings.PRACTICE_INFO);
    }

    public void FinalizeMatch(boolean isDraw){ }

    @Override
    protected void UpdatePortalBrokenStatistics(ArrayList<Player> credits) {}
}
