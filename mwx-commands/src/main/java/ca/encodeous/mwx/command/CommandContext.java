package ca.encodeous.mwx.command;

import ca.encodeous.mwx.mwxcompat1_8.Reflection;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;

public class CommandContext {

    private final Object context;
    private final Object nmsEntity;
    private final Entity entity;

    public CommandContext(Object context) {
        this.context = context;
        Object nmsEntity = null;
        boolean foundField = false;
        for(Field f : context.getClass().getDeclaredFields()) {
            if(f.getType().getTypeName().equals("Entity")) {
                nmsEntity = Reflection.get(f, context);
                foundField = true;
                break;
            }
        }
        if(!foundField) {
            throw new RuntimeException("NMS Entity Field Not Found");
        }
        this.nmsEntity = nmsEntity;
        if(nmsEntity == null) {
            entity = null;
        }else {
            entity = Reflection.invokeMethod("getBukkitEntity", nmsEntity);
        }
    }

    public int GetInteger(String name) {
        return IntegerArgumentType.getInteger((com.mojang.brigadier.context.CommandContext<?>) context, name);
    }

    public double GetDouble(String name) {
        return DoubleArgumentType.getDouble((com.mojang.brigadier.context.CommandContext<?>) context, name);
    }

    public Entity GetEntity() {
        return entity;
    }

    public void SendMessage(String text) {
        GetEntity().sendMessage(text);
    }

}
