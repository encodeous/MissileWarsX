package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mweditCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                if(p.getWorld() == CoreGame.Instance.mwAuto || p.getWorld() == CoreGame.Instance.mwManual){
                    p.sendMessage(Chat.FCL("&aYour changes have been saved!"));
                    p.getWorld().save();
                    LobbyEngine.GetLobby(1).AddPlayer(p);
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

    @Override
    public RootCommand BuildCommand() {
        throw new NotImplementedException("Building this command is not implemented");
    }

    @Override
    public String GetCommandName() {
        return "mwedit";
    }
}
