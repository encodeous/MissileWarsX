package ca.encodeous.mwx.mwxstats;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "players")
public class PlayerStats {
    @DatabaseField(id = true)
    public UUID PlayerId;
    @DatabaseField
    public float TrueSkill = 1200;
    @DatabaseField
    public float TrueSkillCertainty = 100;
    @DatabaseField
    public int Kills;
    @DatabaseField
    public int Deaths;
    @DatabaseField
    public int Wins;
    @DatabaseField
    public int Losses;
    @DatabaseField
    public int Wins1v1;
    @DatabaseField
    public int Losses1v1;
    @DatabaseField
    public int PortalsBroken;
    @DatabaseField
    public int MissilesPlaced;
}
