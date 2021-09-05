package ca.encodeous.mwx.mwxstats;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticManager {
    private String dbString;
    private ConnectionSource source;
    public Dao<PlayerStats, UUID> statsDao;
    public Dao<MatchParticipation, UUID> matchDao;
    public StatisticManager(String databaseName, JavaPlugin plugin) {
        File folder = plugin.getDataFolder();
        File dbFile = new File(folder, databaseName + ".db");
        if(folder.listFiles() != null && Arrays.stream(folder.listFiles())
                .noneMatch(x->x.getName().equals(databaseName+".db"))){
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dbString = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try {
            source = new JdbcConnectionSource(dbString);
            TableUtils.createTableIfNotExists(source, PlayerStats.class);
            TableUtils.createTableIfNotExists(source, MatchParticipation.class);
            statsDao = DaoManager.createDao(source, PlayerStats.class);
            matchDao = DaoManager.createDao(source, MatchParticipation.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
//    public void AddPlayerDeath(Player p, UUID match){
//        KillInfo ki = new KillInfo();
//        ki.Killed = p.getUniqueId();
//        ki.MatchId = match;
//        UUID killer = new UUID(0,0);
//        if(p.getKiller() != null){
//            killer = p.getKiller().getUniqueId();
//            Modify(statsDao, p.getKiller().getUniqueId(), x->{
//                x.Kills++;
//            });
//        }
//        Modify(statsDao, p.getUniqueId(), x->{
//            x.Deaths++;
//        });
//        ki.Killer = killer;
//        try {
//            killDao.create(ki);
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//    }
//    public void MissilePlaced(Player p){
//        Modify(statsDao, p.getUniqueId(), x->{
//            x.MissilesPlaced++;
//        });
//    }
    public void CreatePlayer(Player p){
        try {
            if(!statsDao.idExists(p.getUniqueId())){
                PlayerStats stats = new PlayerStats();
                stats.PlayerId = p.getUniqueId();
                statsDao.create(stats);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public <T, V> void Modify(Dao<T, V> dao, V id, StatsModifier<T> modifier){
        try {
            T obj = dao.queryForId(id);
            modifier.run(obj);
            dao.update(obj);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public void close(){
        try {
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
