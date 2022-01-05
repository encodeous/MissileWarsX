package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSMethod;
import ca.encodeous.simplenms.annotations.NMSStatic;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@NMSClass(type = NMSClass.NMSType.NMS, value = "commands.arguments.coordinates.ArgumentPosition")
public interface ArgumentPosition extends NMSProxy {
    /**
     * Get the position
     * @param context
     * @param name
     * @return
     */
    @NMSStatic
    @NMSMethod
    public BaseBlockPosition a(com.mojang.brigadier.context.CommandContext<?> context, String name)
            throws CommandSyntaxException;
}
