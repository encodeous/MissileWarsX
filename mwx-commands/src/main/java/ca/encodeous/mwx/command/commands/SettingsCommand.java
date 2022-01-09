package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.game.MissileWarsMatchSettingsManager;
import ca.encodeous.mwx.core.game.MissileWarsRankedMatch;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.settings.BooleanSetting;
import ca.encodeous.mwx.core.settings.DoubleSetting;
import ca.encodeous.mwx.core.settings.IntegerSetting;
import ca.encodeous.mwx.core.settings.Setting;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static ca.encodeous.mwx.command.CommandNode.Boolean;
import static ca.encodeous.mwx.command.CommandNode.Double;
import static ca.encodeous.mwx.command.CommandNode.Integer;
import static ca.encodeous.mwx.command.CommandNode.Literal;

public class SettingsCommand extends MissileWarsCommand {

    private int RunCommand(CommandContext context, String settingName, Object v) throws CommandSyntaxException {
        MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetSendingPlayer());
        if(match instanceof MissileWarsRankedMatch) {
            context.SendMessage(Strings.RANKED_PERM_DENIED);
            return 0;
        }
        Setting<?> s = match.settingsManager.getSetting(settingName);
        if(v == null) {
            context.SendMessage("&aCurrent value for &6" + s.getName() + "&a is &6" + s.getValue());
            return 1;
        }
        if(s instanceof BooleanSetting setting) {
            boolean value = (boolean) v;
            setting.setValue(value);
        }else if(s instanceof IntegerSetting setting) {
            int value = (int) v;
            setting.setValue(value);
        }else if(s instanceof DoubleSetting setting) {
            double value = (double) v;
            setting.setValue(value);
        }else return 0;
        context.SendMessage("&aChanged value of &6" + s.getName() + "&a to &6" + s.getValue());
        s.CallUpdateEvent(match);
        return 1;
    }

    @Override
    public RootCommand BuildCommand() {
        RootCommand root = new RootCommand("mwsettings", Command::DefaultRestrictedCommand);
        MissileWarsMatchSettingsManager mw = new MissileWarsMatchSettingsManager(); // gets all settings available for any match
        for(Setting<?> s : mw.getSettings()) {
            CommandNode settingNode = Literal(s.getName()).Executes(context -> RunCommand(context, s.getName(), null));

            if(s instanceof BooleanSetting)
                settingNode.SubCommand(Boolean("value").Executes(context -> RunCommand(context, s.getName(), context.GetBoolean("value"))));
            else if(s instanceof IntegerSetting)
                settingNode.SubCommand(Integer("value").Executes(context -> RunCommand(context, s.getName(), context.GetInteger("value"))));
            else if(s instanceof DoubleSetting)
                settingNode.SubCommand(Double("value").Executes(context -> RunCommand(context, s.getName(), context.GetDouble("value"))));

            root.SubCommand(settingNode);
        }
        return root;
    }

    @Override
    public String GetCommandName() {
        return "mwsettings";
    }
}
