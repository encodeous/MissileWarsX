package ca.encodeous.mwx.mwxcore.missiletrace;

import java.util.HashSet;
import java.util.UUID;

public class TrackedEntity {
    public UUID EntityId;
    public boolean IsRedstoneActivated;
    public HashSet<UUID> Sources = new HashSet<>();
}
