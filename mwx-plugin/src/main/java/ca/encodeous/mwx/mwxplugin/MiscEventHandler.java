package ca.encodeous.mwx.mwxplugin;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kacperduras.protocoltab.manager.PacketTablist;

import java.util.concurrent.ConcurrentHashMap;

public class MiscEventHandler implements Listener {
    private static ConcurrentHashMap<Player, Integer> PlayerUpdateTasks = new ConcurrentHashMap<>();
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PacketTablist list = CoreGame.Instance.tabManager.get(player);
        LobbyEngine.BuildTablist(player, list);
        list.update();
        int taskId = Bukkit.getScheduler().runTaskTimer(MissileWarsX.Instance, () -> {
            if(player.isOnline()){
                PacketTablist nList = CoreGame.Instance.tabManager.get(player);
                LobbyEngine.BuildTablist(player, nList);
                nList.update();
            }else{
                int tId = PlayerUpdateTasks.get(player);
                Bukkit.getScheduler().cancelTask(tId);
            }
        }, 20L, 10L).getTaskId();
        PlayerUpdateTasks.put(player, taskId);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeave(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CoreGame.Instance.tabManager.remove(player);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event){
        if(event.getMessage().toLowerCase().contains("restart")){
            event.setCancelled(true);
            event.getPlayer().sendMessage("The server can only be restarted through console");
        } else if(event.getMessage().toLowerCase().contains("reload")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("The server can only be reloaded through console");
        } else if(event.getMessage().toLowerCase().contains("stop")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("The server can only be stopped through console");
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event){
        MissileWarsMatch match = LobbyEngine.FromPlayer(event.getPlayer());
        if(match != null){
            event.getRecipients().clear();
            event.getRecipients().addAll(match.Teams.keySet());
            if(LobbyEngine.FromWorld(event.getPlayer().getWorld()) != null &&
                    match.lobby.lobbyId != LobbyEngine.FromWorld(event.getPlayer().getWorld()).lobby.lobbyId){
                event.getPlayer().sendMessage(Chat.FCL("&cYour message was sent to the lobby you were previously on. Please use /lobby to switch to your current lobby before chatting!"));
            }
        }
    }
}
