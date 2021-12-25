package ca.encodeous.mwx.engines.performance;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.engines.lobby.Lobby;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import ca.encodeous.mwx.core.utils.Chat;
import org.bukkit.Bukkit;

public class TPSMon implements Runnable {
    private boolean isInWarningZone;
    private boolean isInCriticalZone;
    private long lastCritical;
    public static TPSMon Instance = new TPSMon();
    @Override
    public void run() {
        double tps = RealTPS.Instance.getTps();
        if(System.nanoTime() - RealTPS.Instance.lastTick >= 5L * 1000000000){
            tps = 0;
        }
        if(tps <= CoreGame.Instance.mwConfig.TpsCriticalThreshold){
            if(isInCriticalZone){
                if(System.currentTimeMillis() - lastCritical >= 1000 * 15){
                    // tps has not improved after 15 seconds
                    isInCriticalZone = false;
                }
            }
            if(!isInCriticalZone){
                System.out.println("The server is currently lagging severely. MissileWarsX is trying to resolve the problem.");
                lastCritical = System.currentTimeMillis();
                isInWarningZone = true;
                isInCriticalZone = true;
                for(Lobby lobby : LobbyEngine.Lobbies.values()){
                    lobby.SendMessage("&cPlease wait while the server wipes your lobby...");
                    lobby.Match.Map.CleanMap(()->{
                        lobby.SendMessage("&cYour lobby has been cleaned");
                    });
                }
                Bukkit.getScheduler().runTask(CoreGame.Instance.mwPlugin, () ->
                        Bukkit.broadcastMessage(Chat.FCL("&cAttention, the server is experiencing critically low tps. All lobbies will be cleaned.")));
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
