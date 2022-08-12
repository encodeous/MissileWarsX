package ca.encodeous.mwx.engines.lobby;

import ca.encodeous.mwx.data.MatchType;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.core.game.*;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Lobby {
    // match
    public MissileWarsMatch Match;


    // lobby settings
    public boolean isAutoJoin;
    public int teamSize;
    public int lobbyId;
    public boolean isClosed = false;
    public Lobby(boolean isAutoJoin, int teamSize, int lobbyNumber, MatchType type) {
        this.isAutoJoin = isAutoJoin;
        this.teamSize = teamSize;
        this.lobbyId = lobbyNumber;
        switch(type){
            case NORMAL:
                Match = new MissileWarsMatch(this);
                break;
            case RANKED:
                Match = new MissileWarsRankedMatch(this);
                break;
            case PRACTICE:
                Match = new MissileWarsPracticeMatch(this);
                break;
        }
    }
    public HashSet<Player> GetPlayers(){
        HashSet<Player> allPlayers = new HashSet<>();
        for(Player p : Match.None) allPlayers.add(p);
        for(Player p : Match.Red) allPlayers.add(p);
        for(Player p : Match.Green) allPlayers.add(p);
        for(Player p : Match.Spectators) allPlayers.add(p);
        return allPlayers;
    }
    public void AddPlayer(Player p){
        var view = VirtualWorld.of(Match.Map.MswWorld);
        view.addPlayer(p);
        SendMessage(String.format(Strings.PLAYER_JOIN_LOBBY, p.getDisplayName()));
        Match.AddPlayerToTeam(p, PlayerTeam.None);
        p.recalculatePermissions();
    }
    public void RemovePlayer(Player p){
        var view = VirtualWorld.of(Match.Map.MswWorld);
        view.removePlayer(p);
        SendMessage(String.format(Strings.LEAVE_GAME, p.getDisplayName()));
        Match.RemovePlayer(p);
        if(GetPlayers().isEmpty() && (Match.hasStarted || Match.startCounter.isRunning()) && !Match.endCounter.isRunning()){
            Match.EndGame();
        }
        p.recalculatePermissions();
    }
    public void SendMessage(String message){
        for(Player p : GetPlayers()){
            p.sendMessage(Chat.FCL(message));
        }
    }
    public void CloseLobby(Lobby moveTo){
        if(!isClosed){
            isClosed = true;
            if(moveTo != null){
                for(Player p : GetPlayers()){
                    moveTo.AddPlayer(p);
                }
            }else{
                for(Player p : GetPlayers()){
                    p.kickPlayer(Strings.LOBBY_CLOSE);
                }
            }
            Match.Dispose();
        }
    }
}
