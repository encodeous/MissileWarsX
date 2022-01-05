package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSMethod;
import ca.encodeous.simplenms.annotations.NMSStatic;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

@NMSClass(type = NMSClass.NMSType.NMS, value = "commands.arguments.ArgumentEntity")
public interface ArgumentEntity extends NMSProxy {
    /**
     * Get singular player
     * @param context
     * @param name
     * @return
     */
    @NMSStatic
    @NMSMethod
    public NMSEntity e(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    /**
     * Get singular entity
     * @param context
     * @param name
     * @return
     */
    @NMSStatic
    @NMSMethod
    public NMSEntity a(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    /**
     * Get players
     * @param context
     * @param name
     * @return
     */
    @NMSStatic
    @NMSMethod
    public Collection<Object> d(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    public static Collection<Player> GetPlayers(com.mojang.brigadier.context.CommandContext<?> context, String name) throws CommandSyntaxException{
        var coll = NMSCore.getStaticNMSObject(ArgumentEntity.class).d(context, name);
        ArrayList<Player> pl = new ArrayList<>();
        for(var p : coll){
            pl.add((Player) NMSCore.getNMSObject(NMSEntity.class, p).getBukkitEntity());
        }
        return pl;
    }

    /**
     * Get entities
     * @param context
     * @param name
     * @return
     */
    @NMSStatic
    @NMSMethod
    public Collection<NMSEntity> b(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    public static Collection<Entity> GetEntities(com.mojang.brigadier.context.CommandContext<?> context, String name) throws CommandSyntaxException{
        var coll = NMSCore.getStaticNMSObject(ArgumentEntity.class).d(context, name);
        ArrayList<Entity> pl = new ArrayList<>();
        for(var p : coll){
            pl.add(NMSCore.getNMSObject(NMSEntity.class, p).getBukkitEntity());
        }
        return pl;
    }
}
