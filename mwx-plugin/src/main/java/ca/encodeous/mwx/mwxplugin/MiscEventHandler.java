package ca.encodeous.mwx.mwxplugin;

import ca.encodeous.mwx.mwxcore.CoreGame;
import lobbyengine.LobbyEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kacperduras.protocoltab.manager.PacketTablist;
import pl.kacperduras.protocoltab.manager.TabItem;

import java.util.Map;

public class MiscEventHandler implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PacketTablist list = CoreGame.Instance.tabManager.get(player);
        LobbyEngine.BuildTablist(player, list);
        list.update();
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeave(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CoreGame.Instance.tabManager.remove(player);
    }
}
