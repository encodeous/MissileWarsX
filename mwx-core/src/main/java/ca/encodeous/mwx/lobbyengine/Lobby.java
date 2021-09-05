package ca.encodeous.mwx.lobbyengine;

import ca.encodeous.mwx.mwxcore.gamestate.MissileWarsMatch;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
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
    public Lobby(boolean isAutoJoin, int teamSize, int lobbyNumber, boolean isRanked) {
        this.isAutoJoin = isAutoJoin;
        this.teamSize = teamSize;
        this.lobbyId = lobbyNumber;
        Match = new MissileWarsMatch(this, isRanked);
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
        Match.AddPlayerToTeam(p, PlayerTeam.None);
    }
    public void RemovePlayer(Player p){
        SendMessage(Chat.FormatPlayerAction(p, "has left the game."));
        Match.RemovePlayer(p);
    }
    public void SendMessage(Player sourcePlayer, String message){
        for(Player p : GetPlayers()){
            if(sourcePlayer.isOp()){
                p.sendMessage(sourcePlayer.getUniqueId(),
                        Chat.FCL("<"+sourcePlayer.getDisplayName()+"&r> " + message));
            }else{
                p.sendMessage(sourcePlayer.getUniqueId(),
                        Chat.FCL("<"+sourcePlayer.getDisplayName()+"&r> ") + message);
            }
        }
        System.out.println("[mw-lobby-" + lobbyId + "] CHAT - " + sourcePlayer.getName() + ": " + message);
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
                    p.kickPlayer("Lobby has closed");
                }
            }
            Match.Dispose();
        }
    }
}
