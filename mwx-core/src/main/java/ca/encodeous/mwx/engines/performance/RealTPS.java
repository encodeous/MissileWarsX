package ca.encodeous.mwx.engines.performance;

public class RealTPS implements Runnable{
    public static RealTPS Instance = new RealTPS();
    public double getTps(){
        return 1000.0 / (System.currentTimeMillis() - lastTick);
    }
    public long lastTick = System.currentTimeMillis();
    @Override
    public void run() {
        lastTick = System.currentTimeMillis();
    }
}
