package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.ArgumentEntity;
import ca.encodeous.mwx.command.nms.ArgumentPosition;
import ca.encodeous.mwx.command.nms.BaseBlockPosition;
import ca.encodeous.mwx.command.nms.NMSEntity;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Reflection;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Predicate;

public class CommandContext {

    private final com.mojang.brigadier.context.CommandContext<?> context;
    private final CommandSender commandSender;

    public CommandContext(com.mojang.brigadier.context.CommandContext<?> context, Predicate<CommandSender> preExecutePredicate) throws CommandSyntaxException {
        this.context = context;


        Object commandSource = context.getSource();

        Object commandListener = null;
        Class<?> ICommandListener = null;
        for(Field f : commandSource.getClass().getDeclaredFields()) {
            if(f.getType().getSimpleName().equals("ICommandListener")) {
                ICommandListener = f.getType();
                commandListener = Reflection.get(f, commandSource);
            }
        }
        if(commandListener == null) {
            System.err.println("Could not get the CommandListener");
            throw new RuntimeException("Could not get the CommandListener");
        }

        this.commandSender = Reflection.invokeMethod(Reflection.getMethod(ICommandListener, "getBukkitSender", commandSource.getClass()), commandListener, commandSource);

        if(!preExecutePredicate.test(commandSender)){
            throw new DynamicCommandExceptionType((o)-> () -> "Execution precondition not met").create(null);
        }
    }

    private void throwInvokeException(InvocationTargetException e) throws CommandSyntaxException {
        if(e.getTargetException() instanceof RuntimeException checked) {
            throw checked;
        }else {
            if(e.getTargetException() instanceof CommandSyntaxException cmdError) {
                throw cmdError;
            }else e.printStackTrace();
        }
    }

    public int GetInteger(String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    public double GetDouble(String name) {
        return DoubleArgumentType.getDouble(context, name);
    }

    public boolean GetBoolean(String name) {
        return BoolArgumentType.getBool(context, name);
    }

    public String GetString(String name) {
        return StringArgumentType.getString(context, name);
    }

    public Player GetPlayer(String name) throws CommandSyntaxException {
        NMSEntity player = NMSCore.getStaticNMSObject(ArgumentEntity.class).e(context, name);
        return (Player) player.getBukkitEntity();
    }

    public Collection<Player> GetPlayers(String name) throws CommandSyntaxException {
        return ArgumentEntity.GetPlayers(context, name);
    }

    public Entity GetEntity(String name) throws CommandSyntaxException {
        NMSEntity player = NMSCore.getStaticNMSObject(ArgumentEntity.class).a(context, name);
        return player.getBukkitEntity();
    }

    public Collection<Entity> GetEntities(String name) throws CommandSyntaxException {
        return ArgumentEntity.GetEntities(context, name);
    }

    public Location GetPosition(String name, World world) throws CommandSyntaxException {
        BaseBlockPosition pos = NMSCore.getStaticNMSObject(ArgumentPosition.class).a(context, name);
        return new Location(world, pos.a(), pos.b(), pos.c());
    }

    public Location GetPosition(String name) throws CommandSyntaxException {
        return GetPosition(name, GetSendingWorld());
    }

    public World GetSendingWorld(){
        World w = null;
        if(commandSender != null){
            if(commandSender instanceof BlockCommandSender b){
                w = b.getBlock().getWorld();
            }else if(commandSender instanceof Player p){
                w = p.getWorld();
            }else if(commandSender instanceof Entity e){
                w = e.getWorld();
            }
        }
        return w;
    }

    public Entity GetSendingEntity() {
        return (Entity) commandSender;
    }

    public Player GetSendingPlayer() {
        return (Player) commandSender;
    }

    public CommandSender GetSender() {
        return commandSender;
    }


    public void SendMessage(String text) {
        commandSender.sendMessage(Chat.FCL(text));
    }

    public String GetSenderName() {
        return commandSender.getName();
    }

}
