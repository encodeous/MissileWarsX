package ca.encodeous.mwx.engines.performance;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.utils.Chat;
import com.sk89q.worldedit.session.request.Request;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;

public class TPSMon implements Runnable {
    private boolean isInWarningZone;
    private boolean isInCriticalZone;
    private long lastCritical;
    public static TPSMon Instance = new TPSMon();
    private static ArrayDeque<Double> tpsHist = new ArrayDeque<>();
    public static long lastTickDuration = -1;
    public static long lastTick = System.currentTimeMillis();
    public double getTps(){
        if(lastTickDuration == -1){
            return CoreGame.Instance.mwConfig.TpsCriticalThreshold;
        }
        else{
            var v = 1000.0 / lastTickDuration;
            lastTickDuration = -1;
            return v;
        }
    }
    double CalcTPS(){
        double tps = getTps();
        tpsHist.add(tps);
        while(tpsHist.size() > 10){
            tpsHist.removeFirst();
        }
        double sum = 0;
        for(var d : tpsHist){
            sum += d;
        }
        return sum / tpsHist.size();
    }
    @Override
    public void run() {
        var tps = CalcTPS();
        if(System.currentTimeMillis() - lastTick >= 20000){
            tps = 0;
        }
        if(tps <= CoreGame.Instance.mwConfig.TpsCriticalThreshold){
            if(isInCriticalZone){
                if(System.currentTimeMillis() - lastCritical >= 1000 * 15){
                    // tps has not improved after 15 seconds
                    isInCriticalZone = false;
                }
            }
            if(!isInCriticalZone) {
                System.out.println("The server is currently lagging severely. MissileWarsX is trying to resolve the problem.");
                lastCritical = System.currentTimeMillis();
                isInWarningZone = true;
                isInCriticalZone = true;
                for (var req : Request.getAll()) {
                    req.getExtent().cancel();
                }
                Bukkit.getScheduler().runTask(CoreGame.Instance.mwPlugin, () -> {
                    for (Lobby lobby : LobbyEngine.Lobbies.values()) {
                        CoreGame.GetImpl().ClearEntities(lobby.Match.Map.MswWorld);
                    }
                    Bukkit.broadcastMessage(Chat.FCL("&cAttention, the server is experiencing critically low tps. All lobbies will be cleaned."));
                    for (Lobby lobby : LobbyEngine.Lobbies.values()) {
                        lobby.SendMessage("&cPlease wait while the server wipes your lobby...");
                    }
                });
                for (Lobby lobby : LobbyEngine.Lobbies.values()) {
                    lobby.Match.Map.CleanMap(() -> {
                        lobby.SendMessage("&cYour lobby has been cleaned");
                    });
                }
            }
        }
        if(tps <= CoreGame.Instance.mwConfig.TpsWarningThreshold){
            if(!isInWarningZone){
                isInWarningZone = true;
                Bukkit.getScheduler().runTask(CoreGame.Instance.mwPlugin, new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.broadcastMessage(Chat.FCL("&cCaution, the server is currently lagging. Gameplay may be affected."));
                    }
                });
            }
        }
        if(Math.min(20, tps - 1) >= CoreGame.Instance.mwConfig.TpsWarningThreshold && isInWarningZone){
            // tps has recovered
            isInWarningZone = false;
            isInCriticalZone = false;
        }
    }
}
