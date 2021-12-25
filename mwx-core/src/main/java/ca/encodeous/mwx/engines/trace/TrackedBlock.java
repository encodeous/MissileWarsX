package ca.encodeous.mwx.engines.trace;

import ca.encodeous.mwx.data.TraceType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class TrackedBlock {
    public Vector Position;
    public HashSet<UUID> Sources = new HashSet<>();
    public TraceType Type;
}
