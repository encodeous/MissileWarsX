package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.settings.BooleanSetting;
import ca.encodeous.mwx.core.settings.DoubleSetting;
import ca.encodeous.mwx.core.settings.IntegerSetting;
import ca.encodeous.mwx.core.settings.Setting;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;

import static ca.encodeous.mwx.command.CommandNode.Double;
import static ca.encodeous.mwx.command.CommandNode.Integer;
import static ca.encodeous.mwx.command.CommandNode.*;

import java.lang.String;

public class CosmeticCommand extends MissileWarsCommand {
    @Override
    public RootCommand BuildCommand() {
        RootCommand root = new RootCommand("mwcosmetics", Command::DefaultPlayerCommand, "mwc", "toggle");
        root.SubCommand(Literal("lobby").Executes(ExecutionSource.PLAYER, ctx -> {
            var player = ctx.GetSendingPlayer();
            var setting = LobbyEngine.getSettings(player);
            setting.autoHideLobbySetting = !setting.autoHideLobbySetting;
            if(setting.autoHideLobbySetting){
                ctx.SendMessage(String.format(Strings.FEATURE_HIDDEN, "lobby"));
            }else{
                ctx.SendMessage(String.format(Strings.FEATURE_SHOWN, "lobby"));
            }
            LobbyEngine.refreshCosmetics(player);
            return 1;
        }));
        return root;
    }

    @Override
    public String GetCommandName() {
        return "mwsettings";
    }
}
