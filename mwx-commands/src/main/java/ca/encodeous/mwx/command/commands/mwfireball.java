package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Location;

import static ca.encodeous.mwx.command.CommandSubCommand.Position3d;

public class mwfireball extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwfireball", Command::DefaultRestrictedCommand, "mwf")
                .SubCommand(Position3d("location").Executes((context) -> {
                    MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
                    if(match == null) {
                        context.SendMessage("&cYou cannot summon a fireball");
                        return 0;
                    }
                    Location loc = context.GetPosition("location");
                    match.DeployFireball(loc.getBlock(), new Ref<>(false), new Ref<>(false), context.GetPlayer());
                    context.SendMessage(Chat.FCL("&aFireball summoned at the specified location."));
                    return 1;
                }));
    }

    @Override
    public String GetCommandName() {
        return "mwfireball";
    }
}
