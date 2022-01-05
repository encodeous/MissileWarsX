package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.CommandListenerWrapper;
import ca.encodeous.mwx.core.utils.Reflection;
import ca.encodeous.mwx.core.utils.Utils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface Command {
    int Execute(CommandContext context) throws CommandSyntaxException;

    public static boolean DefaultPlayerCommand(CommandListenerWrapper source){
        return true;
    }

    public static boolean DefaultRestrictedCommand(CommandListenerWrapper source){
        if(source instanceof Player p){
            return Utils.CheckPrivPermissionSilent(p);
        }
        return true;
    }

    public static boolean DefaultAdminCommand(CommandListenerWrapper source){
        if(source instanceof Player p){
            return p.hasPermission("mwx.admin");
        }
        return true;
    }
}
