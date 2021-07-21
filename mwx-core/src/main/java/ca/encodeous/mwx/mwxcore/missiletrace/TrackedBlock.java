package ca.encodeous.mwx.mwxcore.missiletrace;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class TrackedBlock {
    public Vector Position;
    public HashSet<UUID> Sources = new HashSet<>();
    public TraceType Type;
}
