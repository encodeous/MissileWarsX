package ca.encodeous.mwx.engines.trace;

import java.util.HashSet;
import java.util.UUID;

public class TrackedEntity {
    public UUID EntityId;
    public boolean IsRedstoneActivated;
    public HashSet<UUID> Sources = new HashSet<>();
}
