package ca.encodeous.mwx.mwxcore.utils;

import ca.encodeous.mwx.mwxcore.CoreGame;
import org.bukkit.Bukkit;

public class TPSMon implements Runnable {
    private boolean isInWarningZone;
    private boolean isInCriticalZone;
    private long lastCritical;
    @Override
    public void run() {
        double tps = CoreGame.Instance.TpsChecker.getTps();
        if(System.nanoTime() - CoreGame.Instance.TpsChecker.lastTick >= 5L * 1000000000){
            tps = 0;
        }
        if(tps <= CoreGame.Instance.mwConfig.TpsCriticalThreshold){
            if(isInCriticalZone){
                if(System.currentTimeMillis() - lastCritical >= 1000 * 30){
                    // tps has not improved after 30 seconds
                    isInCriticalZone = false;
                }
            }
            if(!isInCriticalZone){
                System.out.println("The server is currently lagging severely. MissileWarsX is trying to resolve the problem.");
                lastCritical = System.currentTimeMillis();
                isInWarningZone = true;
                isInCriticalZone = true;
                Bukkit.getScheduler().runTask(CoreGame.Instance.mwPlugin, new Runnable() {
                    @Override
                    public void run() {
                        CoreGame.Instance.EndMatch();
                        Bukkit.broadcastMessage(Formatter.FCL("&cAttention, the server is experiencing critically low tps. The current game will be reset immediately."));
                    }
                });
            }
        }
        if(tps <= CoreGame.Instance.mwConfig.TpsWarningThreshold){
            if(!isInWarningZone){
                isInWarningZone = true;
                Bukkit.getScheduler().runTask(CoreGame.Instance.mwPlugin, new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.broadcastMessage(Formatter.FCL("&cCaution, the server is currently lagging. Gameplay may be affected."));
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
