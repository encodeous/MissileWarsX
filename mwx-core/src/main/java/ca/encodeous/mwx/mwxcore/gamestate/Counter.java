package ca.encodeous.mwx.mwxcore.gamestate;

import ca.encodeous.mwx.mwxcore.CoreGame;
import org.bukkit.Bukkit;

public class Counter {
    public Counter(Countable countable, int interval, int repetitions) {
        this.countable = countable;
        this.interval = interval;
        this.repetitions = repetitions;
    }

    private Countable countable;
    private int interval;
    private int repetitions;
    private int count = 0;
    private int taskId = -1;
    public void Start(){
        if(taskId == -1){
            Counter counter = this;
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoreGame.Instance.mwPlugin, new Runnable() {
                @Override
                public void run() {
                    countable.Count(counter, count++);
                    if(count == repetitions){
                        StopCounting();
                        countable.FinishedCount(counter);
                    }
                }
            }, 0, interval);
        }
    }
    public void StopCounting(){
        if(taskId != -1){
            Bukkit.getScheduler().cancelTask(taskId);
            count = 0;
            taskId = -1;
        }
    }
    public int GetCount(){
        return count;
    }
}
