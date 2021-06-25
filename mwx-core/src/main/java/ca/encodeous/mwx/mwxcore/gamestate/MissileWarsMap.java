package ca.encodeous.mwx.mwxcore.gamestate;

import ca.encodeous.mwx.mwxcore.utils.Bounds;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;

public class MissileWarsMap {
    /**
     * Should there be a red/green join, or just one single join
     */
    public boolean SeparateJoin;
    public HashSet<Vector> RedJoin;
    public HashSet<Vector> GreenJoin;
    /**
     * Join box for automatic team selection
     */
    public HashSet<Vector> AutoJoin;
    /**
     * Return to lobby signs
     */
    public HashSet<Vector> ReturnToLobby;
    /**
     * Spectate Signs
     */
    public HashSet<Vector> Spectate;
    /**
     * Set players to face a direction
     */
    public float SpawnYaw;

    /**
     * Set players to face a direction
     */
    public float GreenYaw;

    /**
     * Set players to face a direction
     */
    public float RedYaw;


    public Vector Spawn;
    public Vector RedSpawn;
    public Vector RedLobby;
    public Vector GreenSpawn;
    public Vector GreenLobby;

    public Bounds GreenPortal;
    public Bounds RedPortal;

    public World MswWorld;
}
