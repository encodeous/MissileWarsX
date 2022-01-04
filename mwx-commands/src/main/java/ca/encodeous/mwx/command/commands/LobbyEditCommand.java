package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyEditCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                EditLobby(p, args.length == 0? null : args[0]);
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    public void EditLobby(Player p, String arg){
        if(p.getWorld() == CoreGame.Instance.mwAuto || p.getWorld() == CoreGame.Instance.mwManual){
            p.sendMessage(Chat.FCL("&aYour changes have been saved!"));
            p.getWorld().save();
            LobbyEngine.GetLobby(1).AddPlayer(p);
        }else{
            if(arg.equals("auto")){
                if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                p.sendMessage(Chat.FCL("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again."));
                p.teleport(CoreGame.Instance.mwAuto.getSpawnLocation());
            }else if(arg.equals("manual")){
                if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                p.sendMessage(Chat.FCL("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again."));
                p.teleport(CoreGame.Instance.mwManual.getSpawnLocation());
            }
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwedit", "Edits the map template", false).Create(e ->
                e.withArguments(new TextArgument("mapType")
                                .replaceSuggestions(su -> new String[]{"auto", "manual"}))
                        .withPermission(core.GetMwxAdminPermission())
                        .executesPlayer((p, args) -> {
                            EditLobby(p, (String)args[0]);
                        })
        );
    }
}
