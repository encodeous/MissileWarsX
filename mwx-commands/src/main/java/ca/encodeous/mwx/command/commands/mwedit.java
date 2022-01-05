package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.entity.Player;

import static ca.encodeous.mwx.command.CommandSubCommand.Literal;

public class mwedit extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwedit", Command::DefaultAdminCommand)
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
