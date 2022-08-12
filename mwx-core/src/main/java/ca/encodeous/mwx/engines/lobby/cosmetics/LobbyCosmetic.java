package ca.encodeous.mwx.engines.lobby.cosmetics;

import ca.encodeous.mwx.engines.lobby.PlayerCosmeticState;
import ca.encodeous.virtualedit.world.VirtualWorldLayer;
import org.bukkit.entity.Player;

public interface LobbyCosmetic {
    boolean hasDisplayChanged(PlayerCosmeticState setting, Player p);
    boolean renderCheck(PlayerCosmeticState setting, Player p);
    VirtualWorldLayer render(PlayerCosmeticState setting, Player p);
    void postRender(PlayerCosmeticState setting, Player p);
}
