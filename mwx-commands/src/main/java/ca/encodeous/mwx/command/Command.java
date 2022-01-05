package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.CommandListenerWrapper;
import ca.encodeous.mwx.core.utils.Reflection;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface Command {
    int Execute(CommandContext context) throws CommandSyntaxException;

    public static boolean DefaultPlayerCommand(CommandListenerWrapper source){
        return false;
    }

    public static boolean DefaultRestrictedCommand(CommandListenerWrapper source){
        return false;
    }

    public static boolean DefaultAdminCommand(CommandListenerWrapper source){
        return false;
    }
}
