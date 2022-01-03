package ca.encodeous.mwx.command;

@FunctionalInterface
public interface Command {
    int Execute(CommandContext context);


    public static boolean PermissionLevel(Object source, int level) {
        return true;
//            return Reflection.invokeMethod(Reflection.getMethod(source.getClass(), "hasPermission", Integer.class), level);
    }
    public static boolean HighestPermissionLevel(Object source) {return PermissionLevel(source, 4);}
    public static boolean FunctionPermissionLevel(Object source) {return PermissionLevel(source, 2);}
    public static boolean AnyPermissionLevel(Object source) {return PermissionLevel(source, 0);}
}
