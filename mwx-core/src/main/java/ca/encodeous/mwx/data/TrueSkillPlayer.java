package ca.encodeous.mwx.data;

import de.gesundkrank.jskills.IPlayer;

import java.util.Objects;
import java.util.UUID;

public class TrueSkillPlayer implements IPlayer {
    public TrueSkillPlayer(UUID id) {
        this.id = id;
    }

    public UUID id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrueSkillPlayer that = (TrueSkillPlayer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
