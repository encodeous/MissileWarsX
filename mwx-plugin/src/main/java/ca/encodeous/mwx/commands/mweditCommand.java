package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.mwxcore.CoreGame;
import ca.encodeous.mwx.mwxcore.gamestate.PlayerTeam;
import ca.encodeous.mwx.mwxcore.utils.Chat;
import lobbyengine.LobbyEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mweditCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                if(p.getWorld() == CoreGame.Instance.mwAuto || p.getWorld() == CoreGame.Instance.mwManual){
                    p.sendMessage(Chat.FCL("&aYour changes have been saved!"));
                    p.getWorld().save();
                    LobbyEngine.GetLobby("default").AddPlayer(p);
                }else{
                    if(args[0].equals("auto")){
                        if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                        p.sendMessage(Chat.FCL("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again."));
                        p.teleport(CoreGame.Instance.mwAuto.getSpawnLocation());
                    }else if(args[0].equals("manual")){
                        if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                        p.sendMessage(Chat.FCL("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again."));
                        p.teleport(CoreGame.Instance.mwManual.getSpawnLocation());
                    }else return false;
                }

                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}
