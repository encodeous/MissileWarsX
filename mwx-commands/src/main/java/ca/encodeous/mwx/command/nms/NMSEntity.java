package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSMethod;
import org.bukkit.entity.Entity;

@NMSClass(type = NMSClass.NMSType.NMS, value = "world.entity.Entity")
public interface NMSEntity extends NMSProxy {
    @NMSMethod
    public Entity getBukkitEntity();
}
