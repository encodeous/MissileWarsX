package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static ca.encodeous.mwx.command.CommandSubCommand.Literal;

public class mwedit extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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
        return new RootCommand("mwedit", Command::HighestPermissionLevel)
                .SubCommand(Literal("auto").Executes((context) -> {
                    Player p = context.GetPlayer();
                    if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                    p.sendMessage("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again.");
                    p.teleport(CoreGame.Instance.mwAuto.getSpawnLocation());
                    return 1;
                }))
                .SubCommand(Literal("manual").Executes((context) -> {
                    Player p = context.GetPlayer();
                    if(LobbyEngine.FromPlayer(p) != null) LobbyEngine.FromPlayer(p).RemovePlayer(p);
                    p.sendMessage("&aYou are now editing the map. Your changes will be saved once the server shuts down, or run this command again.");
                    p.teleport(CoreGame.Instance.mwManual.getSpawnLocation());
                    return 1;
                }))
                .SubCommand(Literal("finish").Executes((context) -> {
                    if(context.GetPlayer().getWorld() == CoreGame.Instance.mwAuto || context.GetPlayer().getWorld() == CoreGame.Instance.mwManual) {
                        context.SendMessage("&aYour changes have been saved!");
                        context.GetPlayer().getWorld().save();
                        LobbyEngine.GetLobby(1).AddPlayer(context.GetPlayer());
                        return 1;
                    }else {
                        context.SendMessage("&cYou are not in auto/manual missile template world!");
                        return 0;
                    }
                }));
    }

    @Override
    public String GetCommandName() {
        return "mwedit";
    }
}
