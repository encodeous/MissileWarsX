package ca.encodeous.mwx.lobbyengine;

import ca.encodeous.mwx.mwxcore.gamestate.*;
import ca.encodeous.mwx.mwxcore.lang.Strings;
import ca.encodeous.mwx.mwxcore.utils.Chat;
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
        SendMessage(String.format(Strings.PLAYER_JOIN_LOBBY, p.getDisplayName()));
        Match.AddPlayerToTeam(p, PlayerTeam.None);
    }
    public void RemovePlayer(Player p){
        SendMessage(String.format(Strings.LEAVE_GAME, p.getDisplayName()));
        Match.RemovePlayer(p);
        if(GetPlayers().isEmpty() && (Match.hasStarted || Match.startCounter.isRunning()) && !Match.endCounter.isRunning()){
            Match.EndGame();
        }
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
