package ca.encodeous.mwx.command;

import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Reflection;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import static ca.encodeous.mwx.command.Command.ArgumentEntity;
import static ca.encodeous.mwx.command.Command.ArgumentPosition;

public class CommandContext {

    private final com.mojang.brigadier.context.CommandContext<?> context;
    private final Entity entity;
    private final Player player;
    private final CommandSender commandSender;

    public CommandContext(com.mojang.brigadier.context.CommandContext<?> context, boolean requiresEntity, boolean requiresPlayer) throws CommandSyntaxException {
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

        this.commandSender = (CommandSender) Reflection.invokeMethod(Reflection.getMethod(ICommandListener, "getBukkitSender", commandSource.getClass()), commandListener, commandSource);

        Method entityMethod = null, playerMethod = null;
        for(Method m : commandSource.getClass().getDeclaredMethods()) {
            if(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == CommandSyntaxException.class) {
                if(m.getReturnType().getSimpleName().equals("Entity")) {
                    entityMethod = m;
                }else if(m.getReturnType().getSimpleName().equals("EntityPlayer")) {
                    playerMethod = m;
                }
            }
        }

        if(requiresPlayer) {
            if(playerMethod != null) {
                Player pla = null;
                try {
                    pla = Reflection.invokeMethodThrown("getBukkitEntity", Reflection.invokeMethodThrown(playerMethod, commandSource));
                } catch (InvocationTargetException e) {
                    if(e.getTargetException() instanceof RuntimeException checked) {
                        throw checked;
                    }else {
                        if(e.getTargetException() instanceof CommandSyntaxException cmdError) {
                            throw cmdError;
                        }else e.printStackTrace();
                    }
                }
                player = pla;
            }else {
                System.err.println("NMS Player Method Not Found");
                throw new RuntimeException("NMS Player Method Not Found");
            }
        }else player = null;

        if(requiresEntity) {
            if(entityMethod != null) {
                Entity ent = null;
                try {
                    ent = Reflection.invokeMethodThrown("getBukkitEntity", Reflection.invokeMethodThrown(entityMethod, commandSource));
                } catch (InvocationTargetException e) {

                }
                entity = ent;
            }else {
                System.err.println("NMS Entity Method Not Found");
                throw new RuntimeException("NMS Entity Method Not Found");
            }
        }else entity = null;
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

    public String GetString(String name) {
        return StringArgumentType.getString(context, name);
    }

    public Player GetPlayer(String name) throws CommandSyntaxException {
        Object EntityPlayer = null;
        try {
            EntityPlayer = Reflection.invokeMethodThrown(Reflection.getMethod(ArgumentEntity, "e", context.getClass(), String.class), null, context, name);
        } catch (InvocationTargetException e) {
            throwInvokeException(e);
        }
        return (Player) Reflection.invokeMethod(EntityPlayer.getClass(), "getBukkitEntity", EntityPlayer);
    }

    public ArrayList<Player> GetPlayers(String name) throws CommandSyntaxException {
        Collection<Object> CollectionEntityPlayer = null;
        try {
            CollectionEntityPlayer = Reflection.invokeMethodThrown(Reflection.getMethod(ArgumentEntity, "d", context.getClass(), String.class), null, context, name);
        } catch (InvocationTargetException e) {
            throwInvokeException(e);
        }
        ArrayList<Player> players = new ArrayList<>();
        for(Object EntityPlayer : CollectionEntityPlayer) {
            players.add((Player) Reflection.invokeMethod(EntityPlayer.getClass(), "getBukkitEntity", EntityPlayer));
        }
        return players;
    }

    public Entity GetEntity(String name) throws CommandSyntaxException {
        Object Entity = null;
        try {
            Entity = Reflection.invokeMethodThrown(Reflection.getMethod(ArgumentEntity, "a", context.getClass(), String.class), null, context, name);
        } catch (InvocationTargetException e) {
            throwInvokeException(e);
        }
        return (Entity) Reflection.invokeMethod(Entity.getClass(), "getBukkitEntity", Entity);
    }

    public ArrayList<Entity> GetEntities(String name) throws CommandSyntaxException {
        Collection<Object> CollectionEntity = null;
        try {
            CollectionEntity = Reflection.invokeMethodThrown(Reflection.getMethod(ArgumentEntity, "b", context.getClass(), String.class), null, context, name);
        } catch (InvocationTargetException e) {
            throwInvokeException(e);
        }
        ArrayList<Entity> entities = new ArrayList<>();
        for(Object Entity : CollectionEntity) {
            entities.add((Entity) Reflection.invokeMethod(Entity.getClass(), "getBukkitEntity", Entity));
        }
        return entities;
    }

    public Location GetPosition(String name, Entity inheritWorldEntity) throws CommandSyntaxException {
        Object BlockPosition = null;
        try {
            BlockPosition = Reflection.invokeMethodThrown(Reflection.getMethod(ArgumentPosition, "a", context.getClass(), String.class), null, context, name);
        } catch (InvocationTargetException e) {
            throwInvokeException(e);
        }
        Class<?> BaseBlockPosition = BlockPosition.getClass().getSuperclass();
        int x = Reflection.get(BaseBlockPosition, "a", BlockPosition);
        int y = Reflection.get(BaseBlockPosition, "b", BlockPosition);
        int z = Reflection.get(BaseBlockPosition, "c", BlockPosition);
        return new Location(inheritWorldEntity.getWorld(), x, y, z);
    }

    public Location GetPosition(String name) throws CommandSyntaxException {
        if(GetEntity() == null && GetPlayer() == null) {
            System.err.println("Cannot use GetPosition() without the entity to inherit the world from.");
            throw new RuntimeException();
        }
        return GetPosition(name, GetPlayer() == null ? GetEntity() : GetPlayer());
    }

    public Entity GetEntity() {
        return entity;
    }

    public Player GetPlayer() {
        return player;
    }

    public void SendMessage(String text) {
        commandSender.sendMessage(Chat.FCL(text));
    }

    public String GetSenderName() {
        return commandSender.getName();
    }

}
