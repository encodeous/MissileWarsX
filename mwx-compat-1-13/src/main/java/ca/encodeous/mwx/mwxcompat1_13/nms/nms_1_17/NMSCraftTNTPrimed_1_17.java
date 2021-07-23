package ca.encodeous.mwx.mwxcompat1_13.nms.nms_1_17;

import ca.encodeous.mwx.mwxcompat1_13.nms.NMSEntityTNTPrimed;
import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSField;
import ca.encodeous.simplenms.annotations.NMSMethod;
import org.bukkit.entity.Entity;

@NMSClass(type = NMSClass.NMSType.CRAFTBUKKIT, value = "entity.CraftTNTPrimed")
public interface NMSCraftTNTPrimed_1_17 extends NMSProxy {
    @NMSMethod
    void setSource(Entity entity);
}
