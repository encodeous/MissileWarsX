package ca.encodeous.mwx.mwxstats;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "players")
public class PlayerStats {
    @DatabaseField(id = true)
    public UUID PlayerId;
    @DatabaseField
    public double TrueSkill = 1475;
    @DatabaseField
    public double TrueSkillDev = 100;
    @DatabaseField
    public int Kills;
    @DatabaseField
    public int Deaths;
    @DatabaseField
    public int Streak;
    @DatabaseField
    public int MaxStreak;
    @DatabaseField
    public int Wins;
    @DatabaseField
    public int Draws;
    @DatabaseField
    public int Losses;
    @DatabaseField
    public int PortalsBroken;
}
