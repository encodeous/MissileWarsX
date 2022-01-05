package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSMethod;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Collection;

@NMSClass(type = NMSClass.NMSType.NMS, value = "commands.arguments.ArgumentEntity")
public interface ArgumentEntity extends NMSProxy {
    /**
     * Get singular player
     * @param context
     * @param name
     * @return
     */
    @NMSMethod
    public NMSEntity e(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    /**
     * Get singular entity
     * @param context
     * @param name
     * @return
     */
    @NMSMethod
    public NMSEntity a(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    /**
     * Get players
     * @param context
     * @param name
     * @return
     */
    @NMSMethod
    public Collection<NMSEntity> d(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;

    /**
     * Get entities
     * @param context
     * @param name
     * @return
     */
    @NMSMethod
    public Collection<NMSEntity> b(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;
}
