package ca.encodeous.mwx.mwxstats;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "matches")
public class MatchParticipation {
    @DatabaseField(id = true)
    public UUID ParticipationId = UUID.randomUUID();
    @DatabaseField
    public UUID MatchId;
    @DatabaseField
    public boolean HasWon;
    @DatabaseField
    public boolean IsRanked;
    @DatabaseField
    public Date EndTime;
    @DatabaseField
    public float DamageDealt;
    @DatabaseField
    public float DamageReceived;
    @DatabaseField
    public int Kills;
    @DatabaseField
    public int Deaths;
    @DatabaseField
    public float TrueSkillBefore;
    @DatabaseField
    public float TrueSkillCertaintyBefore;
    @DatabaseField
    public float TrueSkillAfter;
    @DatabaseField
    public float TrueSkillCertaintyAfter;
}
