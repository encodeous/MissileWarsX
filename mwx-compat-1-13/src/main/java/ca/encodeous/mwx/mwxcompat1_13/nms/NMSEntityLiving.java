package ca.encodeous.mwx.mwxcompat1_13.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSMethod;

@NMSClass(type = NMSClass.NMSType.NMS, value = "EntityLiving")
public interface NMSEntityLiving extends NMSProxy {
    @NMSMethod
    public NMSEntity getHandle();
}
