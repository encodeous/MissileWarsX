package ca.encodeous.mwx.engines.lobby.cosmetics;

import ca.encodeous.mwx.data.Bounds;
import ca.encodeous.mwx.engines.lobby.PlayerCosmeticState;
import ca.encodeous.virtualedit.world.VirtualWorldLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LobbyHiderCosmetic implements LobbyCosmetic{
    private final Bounds bound = new Bounds();
    private final VirtualWorldLayer layer;
    public LobbyHiderCosmetic(){
        bound.stretch(new Vector(-74, 0, 95));
        bound.stretch(new Vector(-140, 158, -95));
        layer = new VirtualWorldLayer(66, 159, 191);
        layer.setBlock(0, 65, 0, 158, 0, 190, Material.AIR);
        layer.setBlock(39, 39, 69, 69, 95, 95, Material.WHITE_STAINED_GLASS);
        layer.translate(-140, 0, -95);
    }
    @Override
    public boolean renderCheck(PlayerCosmeticState setting, Player p) {
        boolean result;
        if(bound.IsInBounds(p.getLocation().toVector())){
            result = false;
        }
        else result = setting.autoHideLobbySetting;
        return result;
    }

    @Override
    public boolean hasDisplayChanged(PlayerCosmeticState setting, Player p) {
        setting.autoHideLobbyState = renderCheck(setting, p);
        return setting.autoHideLobbyPreviousState != setting.autoHideLobbyState;
    }

    @Override
    public VirtualWorldLayer render(PlayerCosmeticState setting, Player p) {
        return layer;
    }

    @Override
    public void postRender(PlayerCosmeticState setting, Player p) {
        setting.autoHideLobbyPreviousState = setting.autoHideLobbyState;
    }
}
