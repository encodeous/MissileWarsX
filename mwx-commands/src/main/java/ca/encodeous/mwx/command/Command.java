package ca.encodeous.mwx.command;

import ca.encodeous.mwx.core.utils.Reflection;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface Command {

    public static final Class<?> ArgumentEntity = Reflection.getNMSClass("commands.arguments.ArgumentEntity");
    public static final Class<?> ArgumentPosition = Reflection.getNMSClass("commands.arguments.coordinates.ArgumentPosition");

    int Execute(CommandContext context) throws CommandSyntaxException;


    public static boolean PermissionLevel(Object source, int level) {
        return Reflection.invokeMethod(Reflection.getMethod(source.getClass(), "hasPermission", int.class), source, level);
    }
    public static boolean HighestPermissionLevel(Object source) {return PermissionLevel(source, 4);}
    public static boolean FunctionPermissionLevel(Object source) {return PermissionLevel(source, 2);}
}
