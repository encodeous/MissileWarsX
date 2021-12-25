package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.core.lang.Strings;
import org.bukkit.entity.Player;

import java.util.ArrayList;

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
